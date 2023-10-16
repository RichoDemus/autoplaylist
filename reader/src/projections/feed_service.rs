use crate::event::event_store::EventStore;
use crate::event::events::Event;
use crate::sled_wrapper::DiskCache;
use crate::types::{Feed, FeedId, FeedName, FeedUrl};
use crate::youtube::youtube_client::YoutubeClient;
use anyhow::Result;
use log::{error, info, warn};
use serde::{Deserialize, Serialize};
use std::collections::{HashMap, HashSet};
use std::sync::{Arc, Mutex};
use tokio::sync::broadcast::error::RecvError;

pub struct FeedService {
    // event_store: Arc<EventStore>,
    ids: Arc<Mutex<HashSet<FeedId>>>,
    feeds: Arc<Mutex<HashMap<FeedId, Feed>>>,
    client: YoutubeClient,
}

impl FeedService {
    pub fn new(event_store: Arc<EventStore>, client: YoutubeClient) -> Self {
        let ids: Arc<Mutex<HashSet<FeedId>>> = Default::default();
        let ids_spawn = ids.clone();
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
                            register_feed(ids_spawn.clone(), feed_id);
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
        Self { feeds, client, ids }
    }
    pub fn feed(&self, feed: &FeedId) -> Option<Feed> {
        let guard = self.ids.lock().unwrap();
        let feed = guard.get(feed);
        if feed.is_none() {
            warn!("Feed {feed:?} not found");
        }
        feed.map(|id| Feed {
            id: id.clone(),
            name: FeedName("RichoDemus".to_string()),
            items: vec![],
        })
    }

    pub async fn url_to_id(&self, url: FeedUrl) -> Result<(FeedId, FeedName)> {
        self.client.feed_id(url).await
    }
}

fn register_feed(ids: Arc<Mutex<HashSet<FeedId>>>, feed_id: FeedId) {
    ids.lock().unwrap().insert(feed_id);
}
