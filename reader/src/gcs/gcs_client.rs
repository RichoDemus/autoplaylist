use core::sync::atomic::Ordering;
use std::sync::Arc;
use std::sync::atomic::Ordering::SeqCst;
use std::sync::atomic::{AtomicBool, AtomicUsize};
use std::time::Duration;

use anyhow::{Context, Result};
use async_once_cell::OnceCell;
use futures::future::try_join_all;
use google_cloud_storage::client::{Storage, StorageControl};
use itertools::Itertools;
use log::{info, warn};

use crate::gcs::filesystem::{read_file, write_file};

static CLIENT: OnceCell<Clients> = OnceCell::new();

#[derive(Clone)]
struct Clients {
    storage: Storage,
    storage_control: StorageControl,
}

async fn clients() -> Clients {
    let clients: &Clients = CLIENT
        .get_or_init(async {
            unsafe {
                std::env::set_var("GOOGLE_APPLICATION_CREDENTIALS", "google-service-key.json");
            }
            Clients {
                storage: Storage::builder().build().await.unwrap(),
                storage_control: StorageControl::builder().build().await.unwrap(),
            }
        })
        .await;

    clients.clone()
}

pub async fn save_event(name: i32, event: &[u8]) -> Result<()> {
    info!("Saving event {}", name);
    let clients = clients().await;
    clients
        .storage
        .write_object(
            "projects/_/buckets/richo-reader",
            format!("events/v2/{}", name),
            bytes::Bytes::copy_from_slice(event),
        )
        .send_buffered()
        .await
        .with_context(|| format!("write event {name}"))?;
    Ok(())
}

pub async fn get_all_events() -> Result<Vec<Vec<u8>>> {
    let clients = clients().await;

    let response = clients
        .storage_control
        .list_objects()
        .set_parent("projects/_/buckets/richo-reader")
        .send()
        .await
        .context("First list objects")?;

    let mut event_names = vec![];
    for item in response.objects {
        if item.name.starts_with("events/v2/") {
            event_names.push(item.name)
        }
    }

    let mut page_token = response.next_page_token;
    while !page_token.is_empty() {
        let item = clients
            .storage_control
            .list_objects()
            .set_parent("projects/_/buckets/richo-reader")
            .set_page_token(page_token.clone())
            .send()
            .await
            .context("Subsequent list objects")?;
        page_token = item.next_page_token;
        for item in item.objects {
            if item.name.starts_with("events/v2/") {
                event_names.push(item.name)
            }
        }
    }

    info!("{} events", event_names.len());
    event_names.sort_unstable_by_key(|e| {
        let id = e.replace("events/v2/", "");
        id.parse::<usize>().expect("this should be ints")
    });
    make_sure_no_events_missing(event_names.clone());

    let total_events = event_names.len();
    let finished_downloads = Arc::new(AtomicUsize::new(0));
    let finished_downloads_print = finished_downloads.clone();
    let downloading = Arc::new(AtomicBool::new(true));
    let downloading_spawn = downloading.clone();

    actix_rt::spawn(async move {
        let mut events_at_last_print = 0;
        while downloading_spawn.load(Ordering::SeqCst) {
            let downloaded = finished_downloads_print.load(Ordering::SeqCst);
            info!(
                "Events downloaded {}/{}. {} e/s",
                downloaded,
                total_events,
                (downloaded - events_at_last_print)
            );
            events_at_last_print = downloaded;
            actix_rt::time::sleep(Duration::from_millis(1000)).await;
        }
    });

    let mut futures = vec![];
    for name in event_names {
        futures.push(download(name, &clients.storage));
        // finished_downloads.fetch_add(1, Ordering::SeqCst);
    }

    let downloaded = try_join_all(futures)
        .await
        .context("Awaiting all download futures")?;

    downloading.store(false, SeqCst);
    Ok(downloaded)
}

fn make_sure_no_events_missing(events: Vec<String>) {
    actix_rt::spawn(async move {
        let events = events
            .into_iter()
            .map(|name| name.replace("events/v2/", ""))
            .flat_map(|id| id.parse::<usize>().ok())
            .sorted_unstable()
            .collect::<Vec<_>>();
        for (x, event_id) in events.into_iter().enumerate() {
            if x != event_id {
                warn!("Missing event {x}");
            }
        }
        info!("Done validating events")
    });
}

async fn download(filename: String, client: &Storage) -> Result<Vec<u8>> {
    if let Some(bytes) = read_file(filename.clone()).await {
        // info!("Already downloaded {filename}");
        Ok(bytes)
    } else {
        info!("Need to download {filename}");
        let mut resp = client
            .read_object("projects/_/buckets/richo-reader", &filename)
            .send()
            .await
            .with_context(|| format!("Download {filename}"))?;
        let mut contents = Vec::new();
        while let Some(chunk) = resp.next().await.transpose()? {
            contents.extend_from_slice(&chunk);
        }

        write_file(filename.clone(), contents.clone()).await?;
        info!("Downloaded and saved {filename}");
        Ok(contents)
    }
}
