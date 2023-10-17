use crate::service::Services;
use crate::types::{ChannelId, ChannelWithoutVideos, UserId, YoutubeChannelUrl};
use actix_http::StatusCode;
use actix_session::{Session, SessionGetError};
use actix_web::web::{Data, Json, Path};
use actix_web::{get, post, HttpResponse};
use anyhow::anyhow;
use log::{info, warn};
use serde_json::Value;

#[get("/v1/feeds")]
pub async fn get_all_feeds(session: Session, services: Data<Services>) -> HttpResponse {
    info!("Get all feeds");
    info!("Session status: {:?}", session.status());
    info!("Session entries: {:?}", session.entries());

    let user_id = if let Ok(user_id) = session.try_into() {
        user_id
    } else {
        warn!("No session cookie");
        return HttpResponse::new(StatusCode::UNAUTHORIZED).into();
    };
    info!("cool session cooke, userid: {user_id:?}");
    return HttpResponse::Ok().json(build_feeds(user_id, services));
}

fn build_feeds(user: UserId, services: Data<Services>) -> Vec<ChannelWithoutVideos> {
    let feeds = services
        .subscriptions_service
        .lock()
        .unwrap()
        .get_feeds(&user);
    let feed_service = services.feed_service.lock().unwrap();
    feeds
        .iter()
        .flat_map(|feed_id| feed_service.channel(feed_id))
        .map(|feed| ChannelWithoutVideos {
            id: feed.id,
            name: feed.name,
            number_of_available_items: feed.items.len(),
        })
        .collect()
}

#[get("/v1/feeds/{id}")]
pub async fn get_feed(
    session: Session,
    services: Data<Services>,
    feed_id: Path<ChannelId>,
) -> HttpResponse {
    info!("get feed {feed_id:?}");
    let feed = services
        .feed_service
        .lock()
        .unwrap()
        .channel(&feed_id)
        .unwrap(); //todo no unwrap

    HttpResponse::Ok().json(feed)
}

#[post("/v1/feeds")]
pub async fn add_feed(
    session: Session,
    json: Json<Value>,
    services: Data<Services>,
) -> HttpResponse {
    info!("add feed: {:?}", json);
    info!("Session status: {:?}", session.status());
    info!("Session entries: {:?}", session.entries());

    let user_id = if let Ok(user_id) = session.try_into() {
        user_id
    } else {
        warn!("No session cookie");
        return HttpResponse::new(StatusCode::UNAUTHORIZED).into();
    };
    let url = YoutubeChannelUrl(json.as_str().unwrap().to_string());
    let (id, _) = services
        .feed_service
        .lock()
        .unwrap()
        .url_to_id(url)
        .await
        .unwrap(); //todo no unwrap
    services
        .subscriptions_service
        .lock()
        .unwrap()
        .subscribe(user_id, id)
        .unwrap();
    return HttpResponse::Ok().into();
}
