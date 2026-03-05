use anyhow::{Context, Result, bail};
use async_once_cell::OnceCell;
use futures::future::try_join_all;
use google_cloud_storage::client::{Storage, StorageControl};
use itertools::Itertools;
use log::info;

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
    make_sure_no_events_missing(event_names.clone())?;

    let futures = event_names
        .into_iter()
        .map(|name| download(name, &clients.storage))
        .collect::<Vec<_>>();

    let downloaded = try_join_all(futures)
        .await
        .context("Awaiting all download futures")?;

    Ok(downloaded)
}

fn make_sure_no_events_missing(events: Vec<String>) -> Result<()> {
    let events = events
        .into_iter()
        .map(|name| name.replace("events/v2/", ""))
        .flat_map(|id| id.parse::<usize>().ok())
        .sorted_unstable()
        .collect::<Vec<_>>();
    for (x, event_id) in events.into_iter().enumerate() {
        if x != event_id {
            bail!("Missing event {x}");
        }
    }
    info!("Done validating events");
    Ok(())
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
