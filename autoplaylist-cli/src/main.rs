use anyhow::*;
use log::{info, LevelFilter};

use crate::auth::{get_access_token, get_new_refresh_token};
use crate::config::{read_refresh_token, write_refresh_token};

mod auth;
mod config;
pub mod spotify;
#[cfg(test)]
mod test;

#[tokio::main]
async fn main() -> Result<()> {
    env_logger::builder()
        .filter_module("autoplaylist_cli", LevelFilter::Info)
        .init();
    let envs = std::env::args();
    let mut envs = envs.skip(1);
    let asd = match envs.next() {
        None => run_cli().await,
        Some(arg) if arg == "--auth" => run_auth().await,
        Some(o) => bail!("unexpected arg: {}", o),
    };

    asd
}

async fn run_cli() -> Result<()> {
    let refresh_token = if let anyhow::Result::Ok(token) = read_refresh_token().await {
        info!("We have a token: {}", token);
        token
    } else {
        let refresh_token = get_new_refresh_token().await?;
        write_refresh_token(refresh_token.as_str()).await?;
        refresh_token
    };

    info!("Now we have a refresh token: {}", refresh_token);
    let access_token = get_access_token(refresh_token.as_str()).await?;
    write_refresh_token(access_token.refresh_token.as_str()).await?;
    info!("And now we have an access token: {:#?}", access_token);

    spotify::autoplaylist_core::do_stuff("Powerwolf", access_token.access_token, None).await?;
    Ok(())
}

async fn run_auth() -> Result<()> {
    let refresh_token = get_new_refresh_token().await?;
    write_refresh_token(refresh_token.as_str()).await?;
    info!("Got refresh token {}", refresh_token);
    Ok(())
}
