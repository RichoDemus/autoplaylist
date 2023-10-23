extern crate core;

use std::env;

use actix_cors::Cors;
use actix_session::storage::CookieSessionStore;
use actix_session::SessionMiddleware;
use actix_web::cookie::{Key, SameSite};
use actix_web::middleware::Logger;
use actix_web::{cookie, web, App, HttpServer};
use anyhow::Context;
use log::LevelFilter;

use crate::endpoints::admin::{download, get_status};
use crate::endpoints::feeds::{add_feed, feed_operation, get_all_feeds, get_videos};
use crate::endpoints::labels::{add_video_to_label, create_label};
use crate::endpoints::serve_assets::static_fie;
use crate::endpoints::user::{create_user, login};
use crate::service::Services;
use crate::sled_wrapper::Mode;

pub mod endpoints;
pub mod event;
mod gcs;
pub mod projections;
pub mod service;
pub mod sled_wrapper;
#[cfg(test)]
pub mod test;
pub mod types;
pub mod youtube;

#[actix_web::main]
async fn main() -> anyhow::Result<()> {
    let _ = env_logger::builder()
        .filter_module("reader", LevelFilter::Info)
        .try_init();

    let secret_key = Key::from(&[0; 64]); // todo use proper key
    let youtube_key = env::var("YOUTUBE_API_KEY").context("Missing ENV YOUTUBE_API_KEY")?;
    let state = web::Data::new(Services::new(None, youtube_key, Mode::Prod, true));
    HttpServer::new(move || {
        App::new()
            .app_data(state.clone())
            .wrap(Logger::default())
            .wrap(
                SessionMiddleware::builder(CookieSessionStore::default(), secret_key.clone())
                    .cookie_secure(false)
                    .cookie_same_site(SameSite::Strict)
                    .session_lifecycle(
                        actix_session::config::PersistentSession::default()
                            .session_ttl(cookie::time::Duration::days(365)),
                    )
                    .build(),
            )
            .wrap(
                Cors::default()
                    .supports_credentials()
                    .allow_any_origin()
                    .allow_any_method()
                    .allow_any_header(),
            )
            .service(create_user)
            .service(login)
            .service(get_all_feeds)
            .service(get_videos)
            .service(add_feed)
            .service(feed_operation)
            .service(download)
            .service(get_status)
            .service(create_label)
            .service(add_video_to_label)
            .route("/{filename:.*}", web::get().to(static_fie))
    })
    .bind(("0.0.0.0", 8080))?
    .run()
    .await?;

    Ok(())
}
