mod auth;
mod config;
mod spotify;

use crate::auth::{get_access_token, get_new_refresh_token};
use crate::config::{read_refresh_token, write_refresh_token};
use anyhow::*;
use log::{info, LevelFilter};
use std::future::Future;

#[tokio::main]
async fn main() -> Result<()> {
    env_logger::builder()
        .filter_module("autoplaylist_cli", LevelFilter::Info)
        .init();
    let mut envs = std::env::args();
    let _ = envs.next();
    let asd = match envs.next() {
        None => run_cli().await,
        Some(arg) if arg == "--auth" => run_auth().await,
        Some(o) => bail!("unexpected arg: {}", o),
    };

    asd
}

async fn run_cli() -> Result<()> {
    let refresh_token = match read_refresh_token().await {
        anyhow::Result::Ok(token) => {
            info!("We have a token: {}", token);
            token
        }
        Err(_) => {
            let refresh_token = get_new_refresh_token().await?;
            write_refresh_token(refresh_token.as_str()).await?;
            refresh_token
        }
    };

    info!("Now we have a refresh token: {}", refresh_token);
    let access_token = get_access_token(refresh_token.as_str()).await?;
    write_refresh_token(access_token.refresh_token.as_str()).await?;
    info!("And now we have an access token: {:#?}", access_token);

    spotify::autoplaylist_core::do_stuff(access_token.access_token).await?;
    Ok(())
}

async fn run_auth() -> Result<()> {
    let refresh_token = get_new_refresh_token().await?;
    write_refresh_token(refresh_token.as_str()).await?;
    info!("Got refresh token {}", refresh_token);
    Ok(())
}