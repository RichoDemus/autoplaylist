use crate::types::{FeedId, FeedName, FeedUrl};
use anyhow::{bail, Context, Result};
use log::warn;
use reqwest::Client;
use serde_json::Value;
use std::env;

pub struct YoutubeClient {
    client: Client,
    key: String,
}

impl YoutubeClient {
    pub fn new() -> Self {
        Self {
            client: Default::default(),
            key: env::var("YOUTUBE_API_KEY").expect("Missing env API_KEY"),
        }
    }

    pub async fn feed_id(&self, feed_url: FeedUrl) -> Result<(FeedId, FeedName)> {
        if feed_url.starts_with("https://www.youtube.com/channel") {
            bail!("https://www.youtube.com/channel urls are not supported right now")
        }
        let resp = self
            .client
            .get("https://www.googleapis.com/youtube/v3/search/")
            .query(&[
                ("key", self.key.as_str()),
                ("part", "snippet"),
                ("type", "channel"),
                ("q", feed_url.0.as_str()),
            ])
            .send()
            .await
            .context("search channel")?
            .json::<Value>()
            .await
            .context("read body")?;

        // info!("Url: {:?}", feed_url);
        // info!("Resp: {:#?}", resp);
        let items = &resp["items"].as_array().context("items")?;
        if items.len() > 1 {
            warn!("More than one channel for url {:?}", feed_url);
        }
        let channel = items.get(0).context("no channel")?;
        let title = channel["snippet"]["channelTitle"]
            .as_str()
            .context("no title")?;
        let id = channel["snippet"]["channelId"]
            .as_str()
            .context("no title")?;

        Ok((FeedId(id.to_string()), FeedName(title.to_string())))
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
            .filter_module("reader", LevelFilter::Info)
            .try_init();

        let client = YoutubeClient::new();

        assert_eq!(
            client
                .feed_id(FeedUrl(
                    "https://www.youtube.com/@BrightWorksTV".to_string()
                ))
                .await
                .unwrap(),
            (
                FeedId("UCIZi8VWcokrX4hG377au_FA".to_string()),
                FeedName("BrightWorksGaming".to_string())
            )
        );
        assert_eq!(
            client
                .feed_id(FeedUrl(
                    "https://www.youtube.com/user/richodemus".to_string()
                ))
                .await
                .unwrap(),
            (
                FeedId("UCyPvQQ-dZmKzh_PrpWmTJkw".to_string()),
                FeedName("RichoDemus".to_string())
            )
        );
        assert_eq!(
            client
                .feed_id(FeedUrl(
                    "https://www.youtube.com/c/ImagingbySony".to_string()
                ))
                .await
                .unwrap(),
            (
                FeedId("UC7McIsZ7v-RdLedtk6d6zRg".to_string()),
                FeedName("Sony | Camera Channel".to_string())
            )
        );
        assert_eq!(
            client
                .feed_id(FeedUrl(
                    "https://www.youtube.com/channel/UCyPvQQ-dZmKzh_PrpWmTJkw".to_string()
                ))
                .await
                .unwrap_err()
                .to_string(),
            "https://www.youtube.com/channel urls are not supported right now"
        );
    }
}
