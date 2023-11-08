use crate::projections::feed_service::feed_service_types::Video;
use crate::sled_wrapper::DiskCache;
use crate::types::{ChannelId, VideoId};
use crate::youtube::youtube_client::YoutubeClient;
use chrono::{DateTime, Utc};

struct VideoDates {
    id: VideoId,
    upload_date: String,
    last_updated: DateTime<Utc>,
}

pub async fn update_statistics(client: YoutubeClient, videos: &DiskCache<ChannelId, Vec<Video>>) {}
