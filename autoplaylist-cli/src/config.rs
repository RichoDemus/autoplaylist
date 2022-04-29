use anyhow::*;
use log::info;
use std::fs::File;
use std::ops::Not;
use std::path::PathBuf;
use tokio::fs;

#[cfg(test)]
fn get_config_dir() -> Result<PathBuf> {
    Ok(PathBuf::from("target"))
}

#[cfg(not(test))]
fn get_config_dir() -> Result<PathBuf> {
    dirs::config_dir().context("Get config dir")
}

pub async fn read_refresh_token() -> Result<String> {
    let config_dir = get_config_dir()?;
    let dir = config_dir.join("autoplaylist-cli");
    // fs::create_dir_all(dir).await.unwrap();
    let path = dir.join("config.txt");
    if path.exists().not() {
        bail!("Config file does not exist");
    }
    let res = fs::read_to_string(path).await;
    res.map_err(anyhow::Error::from).context("read config")
}

pub async fn write_refresh_token(token: &str) -> Result<()> {
    let config_dir = get_config_dir()?;
    let dir = config_dir.join("autoplaylist-cli");
    fs::create_dir_all(dir).await.context("create config dir")?;
    let config_dir = get_config_dir()?;
    let dir = config_dir.join("autoplaylist-cli");
    let path = dir.join("config.txt");

    fs::write(path, token.as_bytes())
        .await
        .map_err(anyhow::Error::from)
        .context("write config")
}

pub async fn clear_refresh_token() -> Result<()> {
    let config_dir = get_config_dir()?;
    let dir = config_dir.join("autoplaylist-cli");
    fs::remove_dir_all(dir)
        .await
        .map_err(anyhow::Error::from)
        .context("clear config")
}

#[cfg(test)]
mod tests {
    use super::*;
    use log::LevelFilter;
    use tokio::fs;
    use tokio::fs::File;
    use tokio::io::AsyncWriteExt;

    #[tokio::test]
    async fn test_config() -> Result<()> {
        env_logger::builder()
            .filter_module("autoplaylist_cli", LevelFilter::Info)
            .init();

        let no_token = read_refresh_token().await;
        assert!(no_token.is_err(), "should not have gotten a token");

        write_refresh_token("cool-token").await?;

        let token = read_refresh_token().await?;
        assert_eq!(token, "cool-token");

        clear_refresh_token().await?;

        let no_token = read_refresh_token().await;
        assert!(no_token.is_err(), "should not have gotten a token");

        Ok(())
    }
}
