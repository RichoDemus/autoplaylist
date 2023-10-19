use crate::sled_wrapper::DiskCache;
use crate::types::{Channel, ChannelId, Video};
use crate::youtube::youtube_client::YoutubeClient;
use anyhow::Context;
use anyhow::Result;

pub async fn download_channel(
    client: &YoutubeClient,
    videos: &DiskCache<ChannelId, Vec<Video>>,
    channel: Channel,
) -> Result<()> {
    let res = client
        .videos(&channel.playlist)
        .await
        .with_context(|| format!("download {:?} ({:?})", channel.name, channel.id))?;
    videos.insert(channel.id, res);
    Ok(())
}
