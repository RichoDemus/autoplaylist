use std::collections::HashMap;

use anyhow::{bail, Context, Result};
use chrono::{DateTime, TimeZone, Utc};
use itertools::Itertools;
use log::{error, info, trace, warn};
use reqwest::{Client, RequestBuilder};
use serde_json::Value;

use crate::projections::feed_service::feed_service_types::Video;
use crate::types::{
    ChannelId, ChannelName, PlaylistId, VideoDuration, VideoId, ViewCount, YoutubeChannelUrl,
};

#[derive(Clone)]
pub struct YoutubeClient {
    client: Client,
    key: String,
    base_url: String,
}

impl YoutubeClient {
    pub fn new(base_url: Option<String>, key: String) -> Self {
        Self {
            client: Default::default(),
            key,
            base_url: base_url.unwrap_or("https://www.googleapis.com".to_string()),
        }
    }

    pub async fn channel_id(
        &self,
        channel_urll: YoutubeChannelUrl,
    ) -> Result<(ChannelId, ChannelName)> {
        if channel_urll.starts_with("https://www.youtube.com/channel") {
            bail!("https://www.youtube.com/channel urls are not supported right now")
        }
        let value = self
            .call_yt(
                self.client
                    .get(format!("{}/youtube/v3/search/", self.base_url))
                    .query(&[
                        ("part", "snippet"),
                        ("type", "channel"),
                        ("q", &*channel_urll),
                    ]),
            )
            .await?;

        let items = &value["items"].as_array().context("items")?;
        if items.len() > 1 {
            warn!("More than one channel for url {:?}", channel_urll);
        }
        let channel = items.get(0).context("no channel")?;
        let title = channel["snippet"]["channelTitle"]
            .as_str()
            .context("no title")?;
        let id = channel["snippet"]["channelId"].as_str().context("no id")?;

        let id = ChannelId(id.to_string());
        let name = ChannelName(title.to_string());
        info!("{} -> {} [{}]", *channel_urll, *name, *id);
        Ok((id, name))
    }

    pub async fn channel(&self, id: &ChannelId) -> Result<(ChannelName, PlaylistId)> {
        let value = self
            .call_yt(
                self.client
                    .get(format!("{}/youtube/v3/channels/", self.base_url))
                    .query(&[("part", "snippet,contentDetails"), ("id", &**id)]),
            )
            .await?;
        let items = &value["items"].as_array().context("parse items")?;
        if items.len() > 1 {
            warn!("More than one channel for id {:?}", id);
        }
        let channel = items.get(0).context("no channel")?;
        let title = channel["snippet"]["title"]
            .as_str()
            .context("No channel title")?;
        let name = ChannelName(title.to_string());
        let playlist_id = channel["contentDetails"]["relatedPlaylists"]["uploads"]
            .as_str()
            .context("No upploads playlist")?;
        let playlist = PlaylistId(playlist_id.to_string());

        Ok((name, playlist))
    }

    pub async fn videos(
        &self,
        id: &PlaylistId,
        page_token: Option<String>,
    ) -> Result<(Vec<Video>, Option<String>)> {
        let mut query = vec![
            ("part", "snippet"),
            ("maxResults", "50"),
            ("playlistId", &**id),
        ];
        if let Some(ref token) = page_token {
            query.push(("page_token", token));
        }

        let value = self
            .call_yt(
                self.client
                    .get(format!("{}/youtube/v3/playlistItems/", self.base_url))
                    .query(query.as_slice()),
            )
            .await?;

        let next_page_token = value["nextPageToken"].as_str().map(|s| s.to_string());
        let videos = value["items"]
            .as_array()
            .context("no items")?
            .iter()
            .map(|item| {
                trace!("Parsing: {item:#?}");
                let id = item["snippet"]["resourceId"]["videoId"]
                    .as_str()
                    .unwrap()
                    .to_string();
                Video {
                    id: VideoId(id.clone()),
                    title: item["snippet"]["title"].as_str().unwrap().to_string(),
                    description: item["snippet"]["description"].as_str().unwrap().to_string(),
                    upload_date: DateTime::parse_from_rfc3339(
                        item["snippet"]["publishedAt"].as_str().unwrap(),
                    )
                    .unwrap()
                    .with_timezone(&Utc),
                    last_updated: Utc.timestamp_nanos(0),
                    duration: VideoDuration("0".to_string()),
                    views: ViewCount(0),
                }
            })
            .collect::<Vec<_>>();
        Ok((videos, next_page_token))
    }

    pub async fn statistics(
        &self,
        videos: Vec<VideoId>,
    ) -> Result<HashMap<VideoId, (ViewCount, VideoDuration)>> {
        let value = self
            .call_yt(
                self.client
                    .get(format!("{}/youtube/v3/videos/", self.base_url))
                    .query(&[
                        ("part", "statistics,contentDetails"),
                        ("maxResults", "50"),
                        ("id", &videos.iter().map(|v| v.0.as_str()).join(",")),
                    ]),
            )
            .await?;

        let stats: HashMap<VideoId, (ViewCount, VideoDuration)> = value["items"]
            .as_array()
            .context("no items")?
            .iter()
            .flat_map(|item| {
                let id = item["id"].as_str().map(|s| s.to_string());
                let duration = item["contentDetails"]["duration"]
                    .as_str()
                    .map(|s| s.to_string());
                let views = item["statistics"]["viewCount"]
                    .as_str()
                    .map(|s| s.parse::<u64>().ok())
                    .flatten();

                match (id.clone(), duration, views) {
                    (Some(id), Some(duration), Some(views)) => {
                        Some(((VideoId(id)), (ViewCount(views), duration.as_str().into())))
                    }
                    _ => {
                        warn!("Failed to get stats for video {id:?}");
                        None
                    }
                }
            })
            .collect::<HashMap<_, _>>();

        Ok(stats)
    }

    async fn call_yt(&self, builder: RequestBuilder) -> Result<Value> {
        let response = builder
            .query(&[("key", self.key.as_str())])
            .send()
            .await
            .context("Call youtube api")?;

        if !response.status().is_success() {
            error!("YT call failed: {:?}", response);
            let body = response.text().await.context("unwrap error")?;
            if body.to_lowercase().contains("quotaexceeded") {
                bail!("Quota Exceeded");
            }
            bail!("other failure: {}", body);
        }
        let value = response.json::<Value>().await.context("read body")?;
        trace!("Response: {value:#?}");
        Ok(value)
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use log::LevelFilter;
    use std::env;

    #[actix_web::test]
    async fn test_parse_different_ur_types() {
        if env::var("YOUTUBE_API_KEY").is_err() {
            //no key, skip test
            return;
        }
        let _ = env_logger::builder()
            .filter_module("reader", LevelFilter::Trace)
            .try_init();

        let client = YoutubeClient::new(None, env::var("YOUTUBE_API_KEY").unwrap());

        assert_eq!(
            client
                .channel_id(YoutubeChannelUrl(
                    "https://www.youtube.com/@BrightWorksTV".to_string()
                ))
                .await
                .unwrap(),
            (
                ChannelId("UCIZi8VWcokrX4hG377au_FA".to_string()),
                ChannelName("BrightWorksGaming".to_string())
            )
        );
        assert_eq!(
            client
                .channel_id(YoutubeChannelUrl(
                    "https://www.youtube.com/user/richodemus".to_string()
                ))
                .await
                .unwrap(),
            (
                ChannelId("UCyPvQQ-dZmKzh_PrpWmTJkw".to_string()),
                ChannelName("RichoDemus".to_string())
            )
        );
        assert_eq!(
            client
                .channel_id(YoutubeChannelUrl(
                    "https://www.youtube.com/c/ImagingbySony".to_string()
                ))
                .await
                .unwrap(),
            (
                ChannelId("UC7McIsZ7v-RdLedtk6d6zRg".to_string()),
                ChannelName("Sony | Camera Channel".to_string())
            )
        );
        assert_eq!(
            client
                .channel_id(YoutubeChannelUrl(
                    "https://www.youtube.com/channel/UCyPvQQ-dZmKzh_PrpWmTJkw".to_string()
                ))
                .await
                .unwrap_err()
                .to_string(),
            "https://www.youtube.com/channel urls are not supported right now"
        );
    }

    #[actix_web::test]
    async fn test_get_channel() {
        if env::var("YOUTUBE_API_KEY").is_err() {
            //no key, skip test
            return;
        }
        let _ = env_logger::builder()
            .filter_module("reader", LevelFilter::Trace)
            .try_init();

        let client = YoutubeClient::new(None, env::var("YOUTUBE_API_KEY").unwrap());

        // let (name, id) = client
        //     .channel(&ChannelId("UCIZi8VWcokrX4hG377au_FA".to_string()))
        //     .await
        //     .unwrap();
        let (name, id) = match client
            .channel(&ChannelId("UCIZi8VWcokrX4hG377au_FA".to_string()))
            .await
        {
            Ok(r) => r,
            Err(e) => panic!("get channel: {:?}", e),
        };
        println!("{:?}", id);
        assert_eq!(name, ChannelName("BrightWorksGaming".to_string()));
        assert_eq!(id, PlaylistId("UUIZi8VWcokrX4hG377au_FA".to_string()));
    }

    #[actix_web::test]
    async fn test_get_videos() {
        if env::var("YOUTUBE_API_KEY").is_err() {
            //no key, skip test
            return;
        }
        let _ = env_logger::builder()
            .filter_module("reader", LevelFilter::Trace)
            .try_init();

        let client = YoutubeClient::new(None, env::var("YOUTUBE_API_KEY").unwrap());

        let (videos, page_token) = client
            .videos(&PlaylistId("UUIZi8VWcokrX4hG377au_FA".to_string()), None)
            .await
            .unwrap();
        let titles1 = videos.into_iter().map(|v| v.title).collect::<Vec<_>>();

        let (videos, _page_token) = client
            .videos(
                &PlaylistId("UUIZi8VWcokrX4hG377au_FA".to_string()),
                page_token,
            )
            .await
            .unwrap();
        let titles2 = videos.into_iter().map(|v| v.title).collect::<Vec<_>>();

        println!("res :{:#?}", titles1);
        println!("res :{:#?}", titles2);
    }

    #[actix_web::test]
    async fn test_get_statistics() {
        if env::var("YOUTUBE_API_KEY").is_err() {
            //no key, skip test
            return;
        }
        let _ = env_logger::builder()
            .filter_module("reader", LevelFilter::Trace)
            .try_init();

        let client = YoutubeClient::new(None, env::var("YOUTUBE_API_KEY").unwrap());

        let result = client
            .statistics(vec![
                VideoId("9qH8krCX4f0".to_string()),
                VideoId("bNCJgh4XMtQ".to_string()),
            ])
            .await
            .unwrap();

        println!("res :{:#?}", result);

        assert_eq!(
            result
                .get(&VideoId("9qH8krCX4f0".to_string()))
                .unwrap()
                .0
                 .0,
            36
        );
        assert_eq!(
            result
                .get(&VideoId("9qH8krCX4f0".to_string()))
                .unwrap()
                .1
                 .0,
            "00:00:22"
        );

        assert_eq!(
            result
                .get(&VideoId("bNCJgh4XMtQ".to_string()))
                .unwrap()
                .0
                 .0,
            47621
        );
        assert_eq!(
            result
                .get(&VideoId("bNCJgh4XMtQ".to_string()))
                .unwrap()
                .1
                 .0,
            "11:54:58"
        );
    }
}
