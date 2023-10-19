use crate::event::event_store::EventStore;
use crate::event::events::Event;
use crate::projections::feed_service_download::download_channel;
use crate::sled_wrapper::DiskCache;
use crate::types::{Channel, ChannelId, ChannelName, Video, YoutubeChannelUrl};
use crate::youtube::youtube_client::YoutubeClient;
use anyhow::{Context, Result};
use log::{error, info, warn};
use std::sync::Arc;
use tokio::sync::broadcast::error::RecvError;

pub struct FeedService {
    channels: DiskCache<ChannelId, Channel>,
    videos: DiskCache<ChannelId, Vec<Video>>,
    client: YoutubeClient,
}

impl FeedService {
    pub fn new(
        event_store: Arc<EventStore>,
        client: YoutubeClient,
        channels: DiskCache<ChannelId, Channel>,
        videos: DiskCache<ChannelId, Vec<Video>>,
    ) -> Self {
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
            videos,
        }
    }
    pub fn channel(&self, channel_id: ChannelId) -> Option<Channel> {
        self.channels.get(channel_id)
    }

    pub fn videos(&self, id: ChannelId) -> Vec<Video> {
        self.videos.get(id).unwrap_or_default()
    }

    pub async fn url_to_id(&self, url: YoutubeChannelUrl) -> Result<(ChannelId, ChannelName)> {
        self.client.channel_id(url).await
    }

    pub async fn download(&self) -> Result<()> {
        info!("Synchronizing all data with youtube");
        for channel in self.channels.values() {
            download_channel(&self.client, &self.videos, channel).await?;
        }
        info!("Done synchronizing data!");
        Ok(())
    }
}

fn register_channel(client: YoutubeClient, channels: DiskCache<ChannelId, Channel>, id: ChannelId) {
    actix_rt::spawn(async move {
        match client.channel(&id).await {
            Err(e) => warn!("Get channel failed for {id:?}: {:?}", e),
            Ok((name, playlist)) => {
                channels.insert(id.clone(), Channel { id, name, playlist });
            }
        }
    });
}
