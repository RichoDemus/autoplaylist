use std::sync::{Arc, Mutex};
use std::time::Duration;

use anyhow::Result;
use chrono::Utc;
use log::info;
use tokio::sync::mpsc::{Receiver, Sender};
use uuid::uuid;

use crate::event::events::Event;
use crate::event::parse;
use crate::gcs::gcs_client;

pub struct EventStore {
    senders: Arc<Mutex<Vec<Sender<Event>>>>,
    next_event_id: Arc<Mutex<i32>>,
    use_gcs: bool,
}

impl EventStore {
    pub fn new(use_gcs: bool) -> Self {
        let senders: Arc<Mutex<Vec<Sender<Event>>>> = Default::default();
        let senders_spawn = senders.clone();
        let next_event_id = Arc::new(Mutex::new(0));
        let next_event_id_spawn = next_event_id.clone();
        if use_gcs {
            actix_rt::spawn(async move {
                loop {
                    let senders = senders_spawn.lock().unwrap();
                    if senders.len() < 5 {
                        info!("Not enough senders: {}", senders.len());
                        actix_rt::time::sleep(Duration::from_millis(10)).await;
                        continue;
                    }
                    info!("Got all senders");

                    break;
                }
                let events = gcs_client::get_all_events().await.unwrap();
                info!("Loaded {} events", events.len());
                let mut num_events = 0;
                for bytes in events {
                    num_events += 1;
                    let event = String::from_utf8(bytes).unwrap();
                    let (_id, event) = event.split_once(',').unwrap();
                    let event = parse::parse(event).unwrap();
                    for sender in &*senders_spawn.lock().unwrap() {
                        while let Err(e) = sender.try_send(event.clone()) {
                            info!("send err: {:?}", e);
                            actix_rt::time::sleep(Duration::from_millis(10)).await;
                        }
                    }
                }
                *next_event_id_spawn.lock().unwrap() = num_events;
                info!(
                    "All old events sent, next event: {:?}",
                    next_event_id_spawn.lock().unwrap()
                );
            });
        }
        Self {
            senders,
            next_event_id,
            use_gcs,
        }
    }
    pub fn receiver(&mut self) -> Receiver<Event> {
        let (sender, receiver) = tokio::sync::mpsc::channel::<Event>(1000);
        self.senders.lock().unwrap().push(sender);
        receiver
    }
    pub async fn publish_event(&mut self, event: Event) -> Result<()> {
        assert_ne!(
            event.id().0,
            uuid!("00000000-0000-0000-0000-000000000000"),
            "no zero uuids allowed"
        );
        assert_ne!(
            event.timestamp(),
            serde_json::from_str::<chrono::DateTime<Utc>>("\"1970-01-01T00:00:00Z\"").unwrap(),
            "no bad timestamps allowed"
        );
        let bytes = event_to_bytes(&event);
        if self.use_gcs {
            gcs_client::save_event(*self.next_event_id.lock().unwrap(), bytes).await?;
        }
        *self.next_event_id.lock().unwrap() += 1;
        for sender in self.senders.lock().unwrap().iter_mut() {
            while let Err(e) = sender.try_send(event.clone()) {
                info!("send err: {:?}", e);
                actix_rt::time::sleep(Duration::from_millis(10)).await;
            }
        }
        Ok(())
    }
}

fn event_to_bytes(event: &Event) -> Vec<u8> {
    let event_str_res = serde_json::to_string(&event).unwrap();
    let event_str_res = format!("{},{}", event.id().0, event_str_res);

    let bytes_res = event_str_res.as_bytes().to_vec();
    bytes_res
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::gcs::filesystem;

    #[actix_rt::test]
    async fn test_streams() {
        let _ = env_logger::builder()
            .filter_module("reader", log::LevelFilter::Trace)
            .try_init();
        let (sender, mut receiver) = tokio::sync::mpsc::channel(10);

        let one = actix_rt::spawn(async move {
            for c in 0..100 {
                while let Err(e) = sender.try_send(c) {
                    info!("{c} send err: {:?}", e);
                    actix_rt::time::sleep(Duration::from_millis(10)).await;
                }
            }
        });

        let two = actix_rt::spawn(async move {
            while let Some(r) = receiver.recv().await {
                info!("recv: {r}");
            }
            info!("Closed");
        });

        one.await.unwrap();
        two.await.unwrap();
    }

    #[actix_rt::test]
    async fn test_serde() {
        let bytes = filesystem::read_file("events/v2/0".to_string())
            .await
            .unwrap();

        let event_str = String::from_utf8(bytes.clone()).unwrap();
        println!("raw: {:?}", event_str);
        let (_id, event) = event_str.split_once(',').unwrap();
        let event: Event = parse::parse(event).unwrap();

        println!("event: {:?}", event);

        let event_str_res = serde_json::to_string(&event).unwrap();
        let event_str_res = format!("{},{}", event.id().0.to_string(), event_str_res);

        let _bytes_res = event_str_res.as_bytes().to_vec();

        // assert_eq!(event_str, event_str_res);
        // assert_eq!(bytes_res, bytes);
    }
}
