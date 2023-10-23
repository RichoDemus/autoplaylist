use core::sync::atomic::Ordering;
use std::sync::atomic::Ordering::SeqCst;
use std::sync::atomic::{AtomicBool, AtomicUsize};
use std::sync::Arc;
use std::time::Duration;

use anyhow::{Context, Result};
use async_once_cell::OnceCell;
use google_cloud_storage::client::google_cloud_auth::credentials::CredentialsFile;
use google_cloud_storage::client::Client;
use google_cloud_storage::client::ClientConfig;
use google_cloud_storage::http::buckets::list::ListBucketsRequest;
use google_cloud_storage::http::objects::download::Range;
use google_cloud_storage::http::objects::get::GetObjectRequest;
use google_cloud_storage::http::objects::list::ListObjectsRequest;
use google_cloud_storage::http::objects::upload::{Media, UploadObjectRequest, UploadType};
use itertools::Itertools;
use log::{info, warn};

use crate::gcs::filesystem::{read_file, write_file};

const CLIENT: OnceCell<Client> = OnceCell::new();

async fn client() -> Client {
    CLIENT
        .get_or_init(async {
            let cred: CredentialsFile =
                CredentialsFile::new_from_file("google-service-key.json".into())
                    .await
                    .unwrap();
            let config = ClientConfig::default()
                .with_credentials(cred)
                .await
                .unwrap();
            Client::new(config)
        })
        .await
        .clone()
}

pub async fn save_event(name: i32, bytes: Vec<u8>) -> Result<()> {
    info!("Saving event {}", name);
    let client = client().await;
    client
        .upload_object(
            &UploadObjectRequest {
                bucket: "richo-reader".into(),
                ..Default::default()
            },
            bytes,
            &UploadType::Simple(Media::new(format!("events/v2/{}", name))),
        )
        .await?;
    Ok(())
}

pub async fn get_all_events() -> Result<Vec<Vec<u8>>> {
    let client = client().await;
    let _res = client
        .list_buckets(&ListBucketsRequest {
            project: "richo-main".into(),
            ..Default::default()
        })
        .await
        .unwrap();

    let mut event_names = vec![];

    let list = client
        .list_objects(&ListObjectsRequest {
            bucket: "richo-reader".into(),
            ..Default::default()
        })
        .await
        .unwrap();

    for item in list.items.unwrap_or_default() {
        if item.name.starts_with("events/v2/") {
            event_names.push(item.name)
        }
    }

    let mut page_token = list.next_page_token;
    while page_token.is_some() {
        let list = client
            .list_objects(&ListObjectsRequest {
                bucket: "richo-reader".into(),
                page_token: page_token.clone(),
                ..Default::default()
            })
            .await
            .unwrap();

        for item in list.items.unwrap_or_default() {
            if item.name.starts_with("events/v2/") {
                event_names.push(item.name)
            }
        }
        page_token = list.next_page_token;
        info!("{} events, page token: {:?}", event_names.len(), page_token);
    }

    info!("{} events", event_names.len());
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

    let mut downloaded = vec![];
    for name in event_names {
        downloaded.push(download(name, &client).await?);
    }

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

#[cfg(test)]
mod tests {
    use std::sync::atomic::AtomicBool;
    use std::sync::atomic::AtomicUsize;
    use std::sync::Arc;
    use std::time::Duration;

    use actix_rt::time::sleep;
    use log::LevelFilter;

    use super::*;

    // #[actix_web::test]
    #[allow(dead_code)]
    async fn test() {
        let _ = env_logger::builder()
            .filter_module("reader::gcs::gcs_client", LevelFilter::Info)
            // .format_timestamp_millis()
            .try_init();

        let client = client().await;

        // std::env::set_var("RUST_LOG", "info");
        // env_logger::init();
        // let cred: CredentialsFile = CredentialsFile::new_from_file("google-service-key.json".into()).await.unwrap();
        // let config = ClientConfig::default().with_credentials(cred).await.unwrap();
        // let client = Client::new(config);
        let _res = client
            .list_buckets(&ListBucketsRequest {
                project: "richo-main".into(),
                ..Default::default()
            })
            .await
            .unwrap();

        let mut event_names = vec![];

        let list = client
            .list_objects(&ListObjectsRequest {
                bucket: "richo-reader".into(),
                ..Default::default()
            })
            .await
            .unwrap();

        for item in list.items.unwrap_or_default() {
            if item.name.starts_with("events/v2/") {
                event_names.push(item.name)
            }
        }

        let mut page_token = list.next_page_token;
        while page_token.is_some() {
            let list = client
                .list_objects(&ListObjectsRequest {
                    bucket: "richo-reader".into(),
                    page_token: page_token.clone(),
                    ..Default::default()
                })
                .await
                .unwrap();

            for item in list.items.unwrap_or_default() {
                if item.name.starts_with("events/v2/") {
                    event_names.push(item.name)
                }
            }
            page_token = list.next_page_token;
            info!("{} events, page token: {:?}", event_names.len(), page_token);
        }

        // info!("{:#?}", event_names.iter().sorted_by_key(|name| {
        //     name.split("/").collect::<Vec<_>>()[2].parse::<i32>().unwrap()
        // }));
        info!("{} events", event_names.len());

        // info!("{:?}", res);
        // list.items.unwrap().into_iter().for_each(|item|{
        // info!("{}", item.name);
        // });

        // for x in 0..10 {
        //     let obj = client.download_object(&GetObjectRequest {
        //         bucket: "richo-reader".into(),
        //         object: format!("events/v2/{x}"),
        //         ..Default::default()
        //     }, &Range::default()).await.unwrap();
        //     error!("{:?}", String::from_utf8(obj));
        // }

        let total_events = event_names.len();
        let finished_downloads = Arc::new(AtomicUsize::new(0));
        let finished_downloads_print = finished_downloads.clone();
        let downloading = AtomicBool::new(true);

        actix_rt::spawn(async move {
            let mut events_at_last_print = 0;
            while downloading.load(Ordering::SeqCst) {
                let downloaded = finished_downloads_print.load(Ordering::SeqCst);
                info!(
                    "Events downloaded {}/{}. {} e/s",
                    downloaded,
                    total_events,
                    (downloaded - events_at_last_print)
                );
                events_at_last_print = downloaded;
                sleep(Duration::from_millis(1000)).await;
            }
        });

        let mut downloaded = vec![];
        for name in event_names {
            downloaded.push(download(name, &client).await);
        }

        // let futures = event_names.into_iter().map(|name| {
        //     async {
        //         let result = download(name, &client).await;
        //         let _old = finished_downloads.fetch_add(1, Ordering::SeqCst);
        //         result
        //     }
        // })
        //     // }).map(|bytes|String::from_utf8(bytes).unwrap())
        //     .collect::<Vec<_>>();
        //
        //
        // let downloaded = join_all(futures).await;

        info!("First {:?}", downloaded.get(0));
        info!("Last {:?}", downloaded.get(downloaded.len() - 1));

        sleep(Duration::from_millis(3000)).await;
    }
}

async fn download(filename: String, client: &Client) -> Result<Vec<u8>> {
    if let Some(bytes) = read_file(filename.clone()).await {
        // info!("Already downloaded {filename}");
        Ok(bytes)
    } else {
        info!("Need to download {filename}");
        let range = Range::default();
        let request = GetObjectRequest {
            bucket: "richo-reader".into(),
            object: filename.clone(),
            ..Default::default()
        };
        let future = client.download_object(&request, &range);
        let downloaded = future
            .await
            .with_context(|| format!("Failed to download {filename} from gcs"))?;

        write_file(filename.clone(), downloaded.clone()).await?;
        info!("Downloaded and saved {filename}");
        Ok(downloaded)
    }
}
