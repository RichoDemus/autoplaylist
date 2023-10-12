use crate::event::event_store::EventStore;
use crate::event::events::Event;
use crate::types::{FeedId, UserId};
use anyhow::Result;
use log::{error, info};
use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use tokio::sync::broadcast::error::RecvError;

pub struct SubscriptionsService {
    event_store: Arc<EventStore>,
    subscriptions: Arc<Mutex<HashMap<UserId, Vec<FeedId>>>>,
}

impl SubscriptionsService {
    pub fn new(event_store: Arc<EventStore>) -> Self {
        let subscriptions: Arc<Mutex<HashMap<UserId, Vec<FeedId>>>> = Default::default();
        let subscriptions_spawn = subscriptions.clone();
        let mut receiver = event_store.receiver();
        actix_rt::spawn(async move {
            loop {
                match receiver.recv().await {
                    Ok(event) => {
                        info!("Received event {event:?}");
                        if let Event::UserSubscribedToFeed {
                            id,
                            timestamp,
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
            subscriptions,
        }
    }
    pub fn subscribe(&mut self, user_id: UserId, feed_id: FeedId) -> Result<()> {
        self.event_store.publish_event(Event::UserSubscribedToFeed {
            id: Default::default(),
            timestamp: Default::default(),
            user_id,
            feed_id,
        })
    }

    pub fn get_feeds(&self, user: &UserId) -> Vec<FeedId> {
        self.subscriptions
            .lock()
            .unwrap()
            .get(user)
            .cloned()
            .unwrap_or_else(|| vec![])
    }
}
