use crate::sled_wrapper::DiskCache;
use crate::types::{Channel, ChannelId, Video};
use crate::youtube::youtube_client::YoutubeClient;
use anyhow::Context;
use anyhow::Result;
use itertools::Itertools;
use log::{info, trace};
use std::collections::HashSet;

pub async fn download_channel(
    client: &YoutubeClient,
    videos: &DiskCache<ChannelId, Vec<Video>>,
    channel: Channel,
) -> Result<()> {
    info!("Downloading {:?}", channel.name);
    let mut already_downloaded_videos = videos.get(channel.id.clone()).unwrap_or_default();
    let already_downloaded_ids = already_downloaded_videos
        .iter()
        .map(|v| v.id.clone())
        .collect::<HashSet<_>>();

    let newest_video = already_downloaded_videos
        .iter()
        .max_by_key(|video| &video.upload_date);

    trace!("newest video: {:?}", newest_video);

    let (mut res, mut next_page_token) = client
        .videos(&channel.playlist, None)
        .await
        .with_context(|| format!("download {:?} ({:?})", channel.name, channel.id))?;
    info!(
        "Downloaded {} videos, token: {:?}",
        res.len(),
        next_page_token
    );
    for new_video in res {
        if already_downloaded_ids.contains(&new_video.id) {
            info!("Video {:?} already downloaded", new_video.id);
            already_downloaded_videos.sort_by_key(|v| v.upload_date.clone());
            already_downloaded_videos.reverse();
            videos.insert(channel.id, already_downloaded_videos);
            return Ok(());
        }
        already_downloaded_videos.push(new_video);
    }

    'download: loop {
        match next_page_token {
            None => break,
            Some(token) => {
                (res, next_page_token) = client
                    .videos(&channel.playlist, Some(token))
                    .await
                    .with_context(|| format!("download {:?} ({:?})", channel.name, channel.id))?;
                info!(
                    "Downloaded {} videos, token: {:?}",
                    res.len(),
                    next_page_token
                );
                for new_video in res {
                    if already_downloaded_ids.contains(&new_video.id) {
                        info!("Video {:?} already downloaded", new_video.id);
                        break 'download;
                    }
                    already_downloaded_videos.push(new_video);
                }
            }
        }
    }

    already_downloaded_videos.sort_by_key(|v| v.upload_date.clone());
    already_downloaded_videos.reverse();
    info!("Saving {} videos", already_downloaded_videos.len());
    videos.insert(channel.id, already_downloaded_videos);
    Ok(())
}
