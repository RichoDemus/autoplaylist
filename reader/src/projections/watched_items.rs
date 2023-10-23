use std::collections::{HashMap, HashSet};
use std::sync::{Arc, Mutex};

use anyhow::Result;

use crate::event::event_store::EventStore;
use crate::event::events::Event;
use crate::types::{ChannelId, UserId, VideoId};

pub struct WatchedVideosService {
    event_store: Arc<Mutex<EventStore>>,
    watched_items: Arc<Mutex<HashMap<UserId, HashMap<ChannelId, HashSet<VideoId>>>>>,
}

impl WatchedVideosService {
    pub fn new(event_store: Arc<Mutex<EventStore>>) -> Self {
        let watched_items: Arc<Mutex<HashMap<UserId, HashMap<ChannelId, HashSet<VideoId>>>>> =
            Default::default();
        let watched_items_spawn = watched_items.clone();
        let mut receiver = event_store.lock().unwrap().receiver();
        actix_rt::spawn(async move {
            while let Some(event) = receiver.recv().await {
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
        });
        Self {
            event_store,
            watched_items,
        }
    }

    pub async fn watch_item(
        &self,
        user_id: UserId,
        feed_id: ChannelId,
        item_id: VideoId,
    ) -> Result<()> {
        self.event_store
            .lock()
            .unwrap()
            .publish_event(Event::UserWatchedItem {
                id: Default::default(),
                timestamp: Default::default(),
                user_id,
                feed_id,
                item_id,
            })
            .await
    }

    pub async fn unwatch_item(
        &self,
        user_id: UserId,
        feed_id: ChannelId,
        item_id: VideoId,
    ) -> Result<()> {
        self.event_store
            .lock()
            .unwrap()
            .publish_event(Event::UserUnwatchedItem {
                id: Default::default(),
                timestamp: Default::default(),
                user_id,
                feed_id,
                item_id,
            })
            .await
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
