use serde::Deserialize;
use std::fs;
use toml;

#[derive(Deserialize)]
pub struct Config {
    pub youtube_api_key: String,
}

impl Config {
    pub fn from_file(path: &str) -> anyhow::Result<Self> {
        let config_str = fs::read_to_string(path)?;
        let config: Config = toml::from_str(&config_str)?;
        Ok(config)
    }
}
