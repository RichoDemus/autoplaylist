use anyhow::Result;
use tokio::fs;
use tokio::fs::File;
use tokio::io::AsyncReadExt;
use uuid::{uuid, Uuid};

use crate::event::events::Event;
use crate::event::parse;
use crate::projections::{subscriptions, user_service, watched_items};
use crate::types::{FeedId, UserId};

pub async fn init() -> Result<()> {
    let events = read_events_from_disk().await?;

    let mut parsed = vec![];
    for event in events {
        let (_id, event) = event.split_once(',').unwrap();

        // println!("{id} - {event}");
        let event = parse::parse(event)?;
        parsed.push(event);
    }

    for event in parsed {
        subscriptions::process_event(&event);
        watched_items::process_event(&event);
    }

    let subscribed_feeds =
        subscriptions::get_subscribed_feeds(&UserId(uuid!("44ad3e54-a977-4242-9d3c-bbee8187514a")));
    println!("Subscribed to {} feeds", subscribed_feeds.len());

    let watched = watched_items::get_watched_items(&(
        UserId((uuid!("44ad3e54-a977-4242-9d3c-bbee8187514a"))),
        FeedId("UC554eY5jNUfDq3yDOJYirOQ".to_string()),
    ));

    println!("{:?}", watched);

    Ok(())
}

async fn read_events_from_disk() -> Result<Vec<String>> {
    let mut dir = fs::read_dir("data/events/v2").await?;

    let mut result = vec![];
    while let Some(child) = dir.next_entry().await? {
        let mut contents = File::open(child.path()).await?;
        let mut buf = String::new();
        contents.read_to_string(&mut buf).await?;
        result.push(buf);
    }
    Ok(result)
}

pub async fn publish_event(event: Event) -> Result<()> {
    // todo save
    subscriptions::process_event(&event);
    watched_items::process_event(&event);
    user_service::process_event(&event);
    Ok(())
}
