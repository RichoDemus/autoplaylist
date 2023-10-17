use crate::types::{ChannelId, ChannelName, PlaylistId, Video, VideoId, YoutubeChannelUrl};
use anyhow::{bail, Context, Result};
use log::{error, info, trace, warn};
use reqwest::{Client, RequestBuilder, StatusCode};
use serde_json::Value;
use std::env;

#[derive(Clone)]
pub struct YoutubeClient {
    client: Client,
    key: String,
    base_url: String,
}

impl YoutubeClient {
    pub fn new() -> Self {
        Self {
            client: Default::default(),
            key: env::var("YOUTUBE_API_KEY").expect("Missing env YOUTUBE_API_KEY"),
            base_url: env::var("YOUTUBE_BASE_DIR")
                .unwrap_or("https://www.googleapis.com".to_string()),
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
                    .query(&[("part", "snippet,contentDetails"), ("id", &*id)]),
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

    pub async fn videos(&self, id: &PlaylistId) -> Result<Vec<Video>> {
        let value = self
            .call_yt(
                self.client
                    .get(format!("{}/youtube/v3/playlistItems/", self.base_url))
                    .query(&[("part", "snippet"), ("playlistId", &*id)]),
            )
            .await?;

        let videos = value["items"]
            .as_array()
            .context("no items")?
            .into_iter()
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
                    upload_date: item["snippet"]["publishedAt"].as_str().unwrap().to_string(),
                    url: format!("https://www.youtube.com/watch?v={}", id),
                }
            })
            .collect::<Vec<_>>();
        Ok(videos)
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
        trace!("Response: {value:?}");
        Ok(value)
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use log::LevelFilter;

    #[actix_web::test]
    async fn test_parse_different_ur_types() {
        if env::var("YOUTUBE_API_KEY").is_err() {
            //no key, skip test
            return;
        }
        let _ = env_logger::builder()
            .filter_module("reader", LevelFilter::Trace)
            .try_init();

        let client = YoutubeClient::new();

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

        let client = YoutubeClient::new();

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

        let client = YoutubeClient::new();

        let videos = client
            .videos(&PlaylistId("UUIZi8VWcokrX4hG377au_FA".to_string()))
            .await
            .unwrap();
        println!("{:?}", videos);
    }
}
