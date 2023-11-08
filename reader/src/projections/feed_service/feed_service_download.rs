use std::collections::{HashSet, VecDeque};

use crate::projections::feed_service::feed_service_types::Video;
use anyhow::Result;
use futures::Stream;
use futures::StreamExt;
use log::{info, trace, warn};
use tokio::pin;

use crate::sled_wrapper::DiskCache;
use crate::types::{Channel, ChannelId, PlaylistId};
use crate::youtube::youtube_client::YoutubeClient;

pub async fn download_channel(
    client: YoutubeClient,
    videos: &DiskCache<ChannelId, Vec<Video>>,
    channel: Channel,
) -> Result<()> {
    let mut already_downloaded_videos = videos.get(channel.id.clone()).unwrap_or_default();
    let already_downloaded_ids = already_downloaded_videos
        .iter()
        .map(|v| v.id.clone())
        .collect::<HashSet<_>>();

    let videos_client = YoutubeVideos {
        client,
        next_page_token: None,
        no_more_videos: false,
        playlist_id: channel.playlist.clone(),
        videos: VecDeque::new(),
    };

    let stream = videos_client.stream();
    pin!(stream);

    let mut new_vids = 0;
    while let Some(video) = stream.next().await {
        if already_downloaded_ids.contains(&video.id) {
            break;
        }
        new_vids += 1;
        already_downloaded_videos.push(video);
    }

    already_downloaded_videos.sort_by_key(|v| v.upload_date.clone());
    already_downloaded_videos.reverse();
    videos.insert(channel.id, already_downloaded_videos);
    info!(
        "Downloaded {new_vids} videos for {} (playlist: {}",
        *channel.name, *channel.playlist
    );

    Ok(())
}

struct YoutubeVideos {
    pub client: YoutubeClient,
    pub playlist_id: PlaylistId,
    pub videos: VecDeque<Video>,
    pub no_more_videos: bool,
    pub next_page_token: Option<String>,
}

impl YoutubeVideos {
    fn stream(self) -> impl Stream<Item = Video> {
        let stream = futures::stream::unfold(self, |mut state| async move {
            if state.videos.is_empty() && !state.no_more_videos {
                trace!("need to fetch more videos");
                match state
                    .client
                    .videos(&state.playlist_id, state.next_page_token.take())
                    .await
                {
                    Ok((videos, next_page_token)) => {
                        for video in videos {
                            trace!("adding vid: {}", video.title);
                            state.videos.push_back(video);
                        }
                        state.next_page_token = next_page_token;
                        if state.next_page_token.is_none() {
                            state.no_more_videos = true;
                        }
                    }
                    Err(e) => {
                        warn!("get yt videos failed: {:?}", e);
                        state.no_more_videos = true;
                    }
                }
            }
            trace!(
                "popping from: {:?}",
                state.videos.iter().map(|vid| vid.title.clone())
            );
            state.videos.pop_front().map(|video| (video, state))
        });
        stream
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use futures::StreamExt;
    use tokio::pin;

    #[actix_rt::test]
    async fn test_streaming_yt_client() {
        if std::env::var("YOUTUBE_API_KEY").is_err() {
            //no key, skip test
            return;
        }
        let _ = env_logger::builder()
            .filter_module("reader", log::LevelFilter::Trace)
            .try_init();

        let client = YoutubeClient::new(None, std::env::var("YOUTUBE_API_KEY").unwrap());

        let videos_client = YoutubeVideos {
            client,
            next_page_token: None,
            no_more_videos: false,
            playlist_id: PlaylistId("UUZdCLmISVMxI2C_NHU0FQKg".to_string()),
            videos: VecDeque::new(),
        };

        let stream = videos_client.stream();
        pin!(stream);

        while let Some(video) = stream.next().await {
            info!("got vid: {}", video.title);
        }
    }
}
