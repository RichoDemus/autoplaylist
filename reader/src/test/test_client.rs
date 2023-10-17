use crate::types::{Channel, ChannelId, ChannelWithoutVideos, Video};
use anyhow::{bail, Context, Result};
use reqwest::Client;
use serde_json::json;

#[derive(Debug)]
pub struct LoginPage {
    port: u16,
    client: Client,
}

impl LoginPage {
    pub fn new(port: u16) -> Self {
        Self {
            port,
            client: Client::new(),
        }
    }
    pub async fn login(self) -> Result<MainPage> {
        let response = self
            .client
            .post(format!("http://localhost:{}/v1/sessions", self.port))
            .json(&json!({"username":"cool-user", "password":"a-password"}))
            .send()
            .await?;

        if response.status().is_client_error() {
            bail!("Failed to log in")
        }

        assert!(
            response.headers().get("set-cookie").is_some(),
            "No session cookie"
        );

        let cookie_str = response
            .headers()
            .get("set-cookie")
            .unwrap()
            .to_str()
            .unwrap()
            .to_string();
        let split = cookie_str.split(";").collect::<Vec<_>>();
        let cookie = split.get(0).unwrap();
        Ok(MainPage::new(self.port, cookie.to_string(), self.client))
    }

    pub async fn login_wrong_password(self) -> Result<()> {
        let response = self
            .client
            .post(format!("http://localhost:{}/v1/sessions", self.port))
            .json(&json!({"username":"cool-user", "password":"wrong-password"}))
            .send()
            .await?;

        if response.status().is_client_error() {
            Ok(())
        } else {
            bail!("Expected error")
        }
    }

    pub async fn create_user(&self) -> Result<()> {
        let response = self
            .client
            .post(format!("http://localhost:{}/v1/users", self.port))
            .json(&json!({"username":"cool-user", "password":"a-password", "inviteCode":"icode"}))
            .send()
            .await?;

        if response.status().is_client_error() {
            bail!("Failed to create user")
        }

        Ok(())
    }
}

#[derive(Debug)]
pub struct MainPage {
    port: u16,
    cookie: String,
    client: Client,
}

impl MainPage {
    fn new(port: u16, cookie: String, client: Client) -> Self {
        Self {
            port,
            cookie,
            client,
        }
    }
    pub async fn add_feed(&self, url: &str) -> Result<()> {
        let response = self
            .client
            .post(format!("http://localhost:{}/v1/feeds", self.port))
            .header("Cookie", self.cookie.as_str())
            .json(&json!(url))
            .send()
            .await?;
        if response.status().is_success() {
            Ok(())
        } else {
            bail!("Failed to add feed: {:?}", response.error_for_status())
        }
    }

    pub async fn download_feeds(&self) -> Result<()> {
        let response = self
            .client
            .post(format!("http://localhost:{}/admin/download", self.port))
            .header("Cookie", self.cookie.as_str())
            .send()
            .await?;
        if response.status().is_success() {
            Ok(())
        } else {
            bail!("Failed to trigger feed download")
        }
    }

    pub async fn get_feeds(&self) -> Result<Vec<ChannelWithoutVideos>> {
        let response = self
            .client
            .get(format!("http://localhost:{}/v1/feeds", self.port))
            .header("Cookie", self.cookie.as_str())
            .send()
            .await?;
        if response.status().is_success() {
            let feeds: Vec<ChannelWithoutVideos> = response
                .json()
                .await
                .context("Parse feedwithoutitems json")?;
            Ok(feeds)
        } else {
            bail!("Failed to get all feeds")
        }
    }

    pub async fn get_videos(&self, feed_id: ChannelId) -> Result<Vec<Video>> {
        let response = self
            .client
            .get(format!(
                "http://localhost:{}/v1/feeds/{}/items",
                self.port, feed_id.0
            ))
            .header("Cookie", self.cookie.as_str())
            .send()
            .await?;
        if response.status().is_success() {
            let feed: Vec<Video> = response.json().await.context("Parse feed json")?;
            Ok(feed)
        } else {
            bail!("Failed to get feed {:?}", response.error_for_status())
        }
    }
}
