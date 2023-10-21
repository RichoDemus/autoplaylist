use crate::event::event_store::EventStore;
use crate::event::events::Event;
use crate::types::{ChannelId, UserId, Video, VideoId};
use anyhow::Result;
use log::{error, info};
use std::collections::{HashMap, HashSet};
use std::sync::{Arc, Mutex};
use tokio::sync::broadcast::error::RecvError;

pub struct WatchedVideosService {
    event_store: Arc<EventStore>,
    watched_items: Arc<Mutex<HashMap<UserId, HashMap<ChannelId, HashSet<VideoId>>>>>,
}

impl WatchedVideosService {
    pub fn new(event_store: Arc<EventStore>) -> Self {
        let watched_items: Arc<Mutex<HashMap<UserId, HashMap<ChannelId, HashSet<VideoId>>>>> =
            Default::default();
        let watched_items_spawn = watched_items.clone();
        let mut receiver = event_store.receiver();
        actix_rt::spawn(async move {
            loop {
                match receiver.recv().await {
                    Ok(event) => {
                        info!("Received event {event:?}");
                        match event {
                            Event::UserWatchedItem {
                                id,
                                timestamp,
                                user_id,
                                feed_id,
                                item_id,
                            } => {
                                watched_items_spawn
                                    .lock()
                                    .unwrap()
                                    .entry(user_id)
                                    .or_default()
                                    .entry(feed_id)
                                    .or_default()
                                    .insert(item_id);
                            }
                            Event::UserUnwatchedItem {
                                id,
                                timestamp,
                                user_id,
                                feed_id,
                                item_id,
                            } => {
                                watched_items_spawn
                                    .lock()
                                    .unwrap()
                                    .entry(user_id)
                                    .or_default()
                                    .entry(feed_id)
                                    .or_default()
                                    .remove(&item_id);
                            }
                            _ => {}
                        }
                    }
                    Err(RecvError::Closed) => {
                        info!("closed");
                        break;
                    }
                    Err(RecvError::Lagged(x)) => {
                        error!("lagged {x}, very bad!");
                    }
                }
            }
        });
        Self {
            event_store,
            watched_items,
        }
    }

    pub fn watch_item(&self, user_id: UserId, feed_id: ChannelId, item_id: VideoId) -> Result<()> {
        self.event_store.publish_event(Event::UserWatchedItem {
            id: Default::default(),
            timestamp: Default::default(),
            user_id,
            feed_id,
            item_id,
        })
    }

    pub fn unwatch_item(
        &self,
        user_id: UserId,
        feed_id: ChannelId,
        item_id: VideoId,
    ) -> Result<()> {
        self.event_store.publish_event(Event::UserUnwatchedItem {
            id: Default::default(),
            timestamp: Default::default(),
            user_id,
            feed_id,
            item_id,
        })
    }

    pub fn watched_items(&self, user: &UserId, channel: &ChannelId) -> HashSet<VideoId> {
        self.watched_items
            .lock()
            .unwrap()
            .get(user)
            .map(|channels| channels.get(channel).cloned())
            .flatten()
            .unwrap_or_default()
    }
}
