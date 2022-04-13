mod auth;

use anyhow::*;
use log::{info, LevelFilter};
use crate::auth::get_new_refresh_token;

#[tokio::main]
async fn main() -> Result<()> {
    env_logger::builder()
        .filter_module("autoplaylist_cli", LevelFilter::Info)
        .init();
    let mut envs = std::env::args();
    let _ = envs.next();
    match envs.next() {
        None => run_cli().await,
        Some(arg) if arg == "--auth" => run_auth().await,
        Some(o) => bail!("unexpected arg: {}", o),
    }

}

async fn run_cli() -> Result<()> {
    println!("run cli");
    //todo remove
    run_auth().await?;
    Ok(())
}

async fn run_auth() -> Result<()> {
    let refresh_token = get_new_refresh_token().await?;
    info!("Got refresh token {}", refresh_token);
    Ok(())
}
