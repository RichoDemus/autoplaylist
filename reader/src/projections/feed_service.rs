use crate::event::event_store::EventStore;
use crate::event::events::Event;
use crate::sled_wrapper::DiskCache;
use crate::types::{Channel, ChannelId, ChannelName, Video, YoutubeChannelUrl};
use crate::youtube::youtube_client::YoutubeClient;
use anyhow::{Context, Result};
use log::{error, info, trace, warn};
use serde::{Deserialize, Serialize};
use std::collections::{HashMap, HashSet};
use std::future::Future;
use std::sync::{Arc, Mutex};
use tokio::sync::broadcast::error::RecvError;

pub struct FeedService {
    // event_store: Arc<EventStore>,
    // ids: Arc<Mutex<HashSet<ChannelId>>>,
    channels: Arc<Mutex<HashMap<ChannelId, Channel>>>,
    videos: Arc<Mutex<HashMap<ChannelId, Vec<Video>>>>,
    client: YoutubeClient,
}

impl FeedService {
    pub fn new(event_store: Arc<EventStore>, client: YoutubeClient) -> Self {
        // let ids: Arc<Mutex<HashSet<ChannelId>>> = Default::default();
        // let ids_spawn = ids.clone();
        let channels: Arc<Mutex<HashMap<ChannelId, Channel>>> = Default::default();
        let channels_spawn = channels.clone();
        let client_spawn = client.clone();
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
                            register_channel(client_spawn.clone(), channels_spawn.clone(), feed_id);
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
            channels,
            client,
            videos: Default::default(),
        }
    }
    pub fn channel(&self, channel_id: &ChannelId) -> Option<Channel> {
        let guard = self.channels.lock().unwrap();
        let feed = guard.get(channel_id);
        if feed.is_none() {
            warn!("Feed {feed:?} not found");
        }
        feed.cloned()
    }

    pub fn videos(&self, id: &ChannelId) -> Vec<Video> {
        self.videos
            .lock()
            .unwrap()
            .get(id)
            .cloned()
            .unwrap_or_default()
    }

    pub async fn url_to_id(&self, url: YoutubeChannelUrl) -> Result<(ChannelId, ChannelName)> {
        self.client.channel_id(url).await
    }

    pub async fn download(&self) -> Result<()> {
        info!("Synchronizing all data with youtube");
        for (id, channel) in self.channels.lock().unwrap().iter() {
            let videos = self
                .client
                .videos(&channel.playlist)
                .await
                .with_context(|| format!("download {:?} ({:?})", channel.name, channel.id))?;
            self.videos.lock().unwrap().insert(id.clone(), videos);
        }
        info!("Done synchronizing data!");
        trace!("Videos: {:?}", self.videos.lock().unwrap());
        Ok(())
    }
}

fn register_channel(
    client: YoutubeClient,
    channels: Arc<Mutex<HashMap<ChannelId, Channel>>>,
    id: ChannelId,
) {
    actix_rt::spawn(async move {
        match client.channel(&id).await {
            Err(e) => warn!("Get channel failed for {id:?}: {:?}", e),
            Ok((name, playlist)) => {
                channels
                    .lock()
                    .unwrap()
                    .insert(id.clone(), Channel { id, name, playlist });
            }
        }
    });
}
