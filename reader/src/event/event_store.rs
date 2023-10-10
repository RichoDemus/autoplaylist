use anyhow::Result;
use log::info;

use crate::event::events::Event;
use crate::event::parse;
use crate::gcs::gcs_client;
use crate::projections::{subscriptions, user_service, watched_items};

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
    subscriptions::process_event(&event);
    watched_items::process_event(&event);
    user_service::process_event(&event);
    Ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[actix_web::test]
    async fn read_and_parse_events() {
        init().await.unwrap()
    }
}
