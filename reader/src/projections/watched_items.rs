use std::collections::{HashMap, HashSet};
use std::sync::{Arc, Mutex};

use anyhow::Result;
use chrono::Utc;
use dashmap::DashMap;

use crate::event::event_store::EventStore;
use crate::event::events::Event;
use crate::types::{ChannelId, UserId, VideoId};

pub struct WatchedVideosService {
    event_store: Arc<Mutex<EventStore>>,
    watched_items: Arc<DashMap<UserId, HashMap<ChannelId, HashSet<VideoId>>>>,
}

impl WatchedVideosService {
    pub fn new(event_store: Arc<Mutex<EventStore>>) -> Self {
        let watched_items: Arc<DashMap<UserId, HashMap<ChannelId, HashSet<VideoId>>>> =
            Default::default();
        let watched_items_spawn = watched_items.clone();
        let mut receiver = event_store.lock().unwrap().receiver();
        actix_rt::spawn(async move {
            while let Some(event) = receiver.recv().await {
                match event {
                    Event::UserWatchedItem {
                        id: _,
                        timestamp: _,
                        user_id,
                        feed_id,
                        item_id,
                    } => {
                        watched_items_spawn
                            .entry(user_id)
                            .or_default()
                            .entry(feed_id)
                            .or_default()
                            .insert(item_id);
                    }
                    Event::UserUnwatchedItem {
                        id: _,
                        timestamp: _,
                        user_id,
                        feed_id,
                        item_id,
                    } => {
                        watched_items_spawn
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
                timestamp: Utc::now(),
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
                timestamp: Utc::now(),
                user_id,
                feed_id,
                item_id,
            })
            .await
    }

    pub fn watched_items(&self, user: &UserId, channel: &ChannelId) -> HashSet<VideoId> {
        self.watched_items
            .get(user)
            .and_then(|channels| channels.get(channel).cloned())
            .unwrap_or_default()
    }
}
