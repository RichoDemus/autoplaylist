use crate::sled_wrapper::DiskCache;
use crate::types::{Channel, ChannelId, Video};
use crate::youtube::youtube_client::YoutubeClient;
use anyhow::Context;
use anyhow::Result;
use itertools::Itertools;
use log::trace;

pub async fn download_channel(
    client: &YoutubeClient,
    videos: &DiskCache<ChannelId, Vec<Video>>,
    channel: Channel,
) -> Result<()> {
    // let already_downloaded_videos = videos.get(channel.id.clone()).unwrap_or_default();
    //
    // let newest_video = already_downloaded_videos
    //     .iter()
    //     .max_by_key(|video| &video.upload_date);
    //
    // trace!("newest video: {:?}", newest_video);

    let mut downloaded_videos = vec![];

    let (mut res, mut next_page_token) = client
        .videos(&channel.playlist, None)
        .await
        .with_context(|| format!("download {:?} ({:?})", channel.name, channel.id))?;
    trace!(
        "Downloaded {} videos, token: {:?}",
        res.len(),
        next_page_token
    );
    downloaded_videos.append(&mut res);

    loop {
        match next_page_token {
            None => break,
            Some(token) => {
                (res, next_page_token) = client
                    .videos(&channel.playlist, Some(token))
                    .await
                    .with_context(|| format!("download {:?} ({:?})", channel.name, channel.id))?;
                trace!(
                    "Downloaded {} videos, token: {:?}",
                    res.len(),
                    next_page_token
                );
                downloaded_videos.append(&mut res);
            }
        }
    }

    // while let Some(token) = next_page_token {
    //     (res, next_page_token) = client
    //         .videos(&channel.playlist, next_page_token)
    //         .await
    //         .with_context(|| format!("download {:?} ({:?})", channel.name, channel.id))?;
    //
    // }
    //
    //
    // let (res2, next_page_token) = client
    //     .videos(&channel.playlist, next_page_token)
    //     .await
    //     .with_context(|| format!("download {:?} ({:?})", channel.name, channel.id))?;
    // trace!("next page token: {:?}", next_page_token);
    // let res = [res, res2].concat();
    // videos.insert(channel.id, res);
    videos.insert(channel.id, downloaded_videos);
    Ok(())
}
