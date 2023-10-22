use crate::event::event_store::EventStore;
use crate::event::events::Event;
use crate::types::{ChannelId, UserId};
use anyhow::Result;
use log::{error, info};
use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use tokio::sync::broadcast::error::RecvError;

pub struct SubscriptionsService {
    event_store: Arc<Mutex<EventStore>>,
    subscriptions: Arc<Mutex<HashMap<UserId, Vec<ChannelId>>>>,
}

impl SubscriptionsService {
    pub fn new(event_store: Arc<Mutex<EventStore>>) -> Self {
        let subscriptions: Arc<Mutex<HashMap<UserId, Vec<ChannelId>>>> = Default::default();
        let subscriptions_spawn = subscriptions.clone();
        let mut receiver = event_store.lock().unwrap().receiver();
        actix_rt::spawn(async move {
            while let Some(event) = receiver.recv().await {
                if let Event::UserSubscribedToFeed {
                    id: _,
                    timestamp: _,
                    user_id,
                    feed_id,
                } = event
                {
                    subscriptions_spawn
                        .lock()
                        .unwrap()
                        .entry(user_id)
                        .or_default()
                        .push(feed_id);
                }
            }
        });
        Self {
            event_store,
            subscriptions,
        }
    }
    pub async fn subscribe(&mut self, user_id: UserId, feed_id: ChannelId) -> Result<()> {
        self.event_store
            .lock()
            .unwrap()
            .publish_event(Event::UserSubscribedToFeed {
                id: Default::default(),
                timestamp: Default::default(),
                user_id,
                feed_id,
            })
            .await
    }

    pub fn get_feeds(&self, user: &UserId) -> Vec<ChannelId> {
        self.subscriptions
            .lock()
            .unwrap()
            .get(user)
            .cloned()
            .unwrap_or_else(|| vec![])
    }
}
