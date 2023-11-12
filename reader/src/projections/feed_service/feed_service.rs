use actix_rt::spawn;
use std::sync::{Arc, Mutex};
use std::time::Duration;

use anyhow::Result;
use chrono::{DateTime, Utc};
use itertools::Itertools;
use log::{info, trace, warn};
use tokio::sync::mpsc::Receiver;
use tokio::time::sleep;

use crate::event::event_store::EventStore;
use crate::event::events::Event;
use crate::gcs::filesystem;
use crate::projections::feed_service::feed_service_download::download_channel;
use crate::projections::feed_service::feed_service_statistics::update_statistics;
use crate::projections::feed_service::feed_service_types::Video;
use crate::sled_wrapper::DiskCache;
use crate::types::{Channel, ChannelId, ChannelName, YoutubeChannelUrl};
use crate::youtube::youtube_client::YoutubeClient;

pub struct FeedService {
    channels: DiskCache<ChannelId, Channel>,
    videos: DiskCache<ChannelId, Vec<Video>>,
    client: YoutubeClient,
}

impl FeedService {
    pub fn new(
        event_store: Arc<Mutex<EventStore>>,
        client: YoutubeClient,
        channels: DiskCache<ChannelId, Channel>,
        videos: DiskCache<ChannelId, Vec<Video>>,
    ) -> Self {
        let channels_spawn = channels.clone();
        let client_spawn = client.clone();
        let mut receiver: Receiver<Event> = event_store.lock().unwrap().receiver();
        spawn(async move {
            while let Some(event) = receiver.recv().await {
                trace!("Received event {event:?}");
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
        #[cfg(not(test))]
        {
            let channels = self.channels.clone();
            let videos = self.videos.clone();
            let client = self.client.clone();
            spawn(async move {
                sleep(Duration::from_secs(1)).await;
                if let Err(e) = maybe_download(channels, videos, client).await {
                    warn!("Failed to do feed triggered DL: {e:?}");
                }
            });
        }
        self.videos.get(id).unwrap_or_default()
    }

    pub async fn url_to_id(&self, url: YoutubeChannelUrl) -> Result<(ChannelId, ChannelName)> {
        self.client.channel_id(url).await
    }

    pub async fn download(&self) -> Result<()> {
        do_download(
            self.channels.clone(),
            self.videos.clone(),
            self.client.clone(),
        )
        .await
    }
}

fn register_channel(client: YoutubeClient, channels: DiskCache<ChannelId, Channel>, id: ChannelId) {
    spawn(async move {
        match client.channel(&id).await {
            Err(e) => warn!("Get channel failed for {id:?}: {:?}", e),
            Ok((name, playlist)) => {
                channels.insert(id.clone(), Channel { id, name, playlist });
            }
        }
    });
}

async fn maybe_download(
    channels: DiskCache<ChannelId, Channel>,
    videos: DiskCache<ChannelId, Vec<Video>>,
    client: YoutubeClient,
) -> Result<()> {
    #[cfg(not(test))]
    {
        let last_updated = filesystem::read_file("last_updated.txt".to_string())
            .await
            .map(String::from_utf8)
            .map(Result::ok)
            .flatten()
            .map(|txt| DateTime::parse_from_rfc3339(txt.as_str()).ok())
            .flatten()
            .map(|d| d.with_timezone(&Utc));
        info!("{last_updated:?}");
        if let Some(last_updated) = last_updated {
            let diff: chrono::Duration = Utc::now() - last_updated;
            if diff > chrono::Duration::hours(4) {
                info!("More than 4 hours ago");
                spawn(async move {
                    if let Err(e) = do_download(channels, videos, client).await {
                        warn!("Failed to do feed triggered DL(2): {e:?}");
                    }
                });
            }
        } else {
            info!("No last updated file");
            spawn(async move {
                if let Err(e) = do_download(channels, videos, client).await {
                    warn!("Failed to do feed triggered DL(2): {e:?}");
                }
            });
        }
    }

    Ok(())
}

async fn do_download(
    channels: DiskCache<ChannelId, Channel>,
    videos: DiskCache<ChannelId, Vec<Video>>,
    client: YoutubeClient,
) -> Result<()> {
    info!("Synchronizing all data with youtube");
    for channel in channels
        .values()
        .sorted_unstable_by(|a, b| Ord::cmp(&a.name.0, &b.name.0))
    {
        if let Err(e) = download_channel(client.clone(), &videos, channel.clone()).await {
            warn!("Channel {} failed: {:?}", &*channel.name, e);
        }
    }
    info!("Done synchronizing data!");
    update_statistics(client, &videos).await;
    #[cfg(not(test))]
    {
        filesystem::write_file(
            "last_updated.txt".to_string(),
            Utc::now().to_rfc3339().into_bytes(),
        )
        .await?;
    }
    Ok(())
}
