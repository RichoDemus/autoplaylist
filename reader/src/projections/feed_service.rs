use crate::event::event_store::EventStore;
use crate::event::events::Event;
use crate::types::{FeedId, FeedName, FeedUrl};
use crate::youtube::youtube_client::YoutubeClient;
use anyhow::Result;
use log::{error, info, warn};
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use tokio::sync::broadcast::error::RecvError;

pub struct FeedService {
    // event_store: Arc<EventStore>,
    feeds: Arc<Mutex<HashMap<FeedId, Feed>>>,
    client: YoutubeClient,
}

impl FeedService {
    pub fn new(event_store: Arc<EventStore>, client: YoutubeClient) -> Self {
        let feeds: Arc<Mutex<HashMap<FeedId, Feed>>> = Default::default();
        let feeds_spawn = feeds.clone();
        let mut receiver = event_store.receiver();
        actix_rt::spawn(async move {
            loop {
                match receiver.recv().await {
                    Ok(event) => {
                        info!("Received event {event:?}");
                        if let Event::UserSubscribedToFeed {
                            id: _,
                            timestamp: _,
                            user_id: _,
                            feed_id,
                        } = event
                        {
                            feeds_spawn.lock().unwrap().insert(
                                feed_id.clone(),
                                Feed {
                                    id: feed_id,
                                    name: FeedName("cool-feed-name".to_string()),
                                },
                            );
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
            // event_store,
            feeds,
            client,
        }
    }
    pub fn feed(&self, feed: &FeedId) -> Option<Feed> {
        let guard = self.feeds.lock().unwrap();
        let feed = guard.get(feed);
        if feed.is_none() {
            warn!("Feed {feed:?} not found");
        }
        feed.cloned()
    }

    pub async fn url_to_id(&self, url: FeedUrl) -> Result<(FeedId, FeedName)> {
        self.client.feed_id(url).await
    }
}

#[derive(Debug, Clone, Serialize, Deserialize, Eq, PartialEq)]
pub struct Feed {
    pub id: FeedId,
    pub name: FeedName,
}
