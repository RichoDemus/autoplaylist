use anyhow::{Context, Result};
use log::info;
use tokio::sync::broadcast;
use tokio::sync::broadcast::{Receiver, Sender};

use crate::event::events::Event;
use crate::event::parse;
use crate::gcs::gcs_client;
use crate::projections::{user_service, watched_items};

pub async fn init() -> Result<()> {
    let events = gcs_client::load_events_from_gcs_and_disk().await?;

    info!("Loaded {} events", events.len());
    for event in events {
        let event = String::from_utf8(event)?;
        let (_id, event) = event.split_once(',').unwrap();

        // println!("{id} - {event}");
        let event = parse::parse(event)?;
        publish_event(event, false)?;
    }

    Ok(())
}

pub fn publish_event(event: Event, _also_persist: bool) -> Result<()> {
    // todo save
    // subscriptions::process_event(&event);
    watched_items::process_event(&event);
    user_service::process_event(&event);
    Ok(())
}

#[cfg(test)]
mod tests {
    // use super::*;

    // #[actix_web::test]
    // async fn read_and_parse_events() {
    //     init().await.unwrap()
    // }
}

//// NEW HERE

pub struct EventStore {
    sender: Sender<Event>,
}

impl EventStore {
    pub fn new() -> Self {
        let (sender, _receiver) = broadcast::channel(100);
        Self { sender }
    }
    pub fn receiver(&self) -> Receiver<Event> {
        self.sender.subscribe()
    }
    pub fn publish_event(&self, event: Event) -> Result<()> {
        let _receivers = self.sender.send(event).context("Broadcast event")?;
        Ok(())
    }
}
