use crate::service::Services;
use crate::types::{FeedId, FeedUrl, FeedWithoutItem, UserId};
use actix_http::StatusCode;
use actix_session::Session;
use actix_web::web::{Data, Json};
use actix_web::{get, post, HttpResponse};
use log::{info, warn};
use serde_json::Value;

#[get("/v1/feeds")]
pub async fn get_all_feeds(session: Session, services: Data<Services>) -> HttpResponse {
    info!("Get all feeds");
    info!("Session status: {:?}", session.status());
    info!("Session entries: {:?}", session.entries());

    let user_id = if let Ok(Some(user_id)) = session.get::<UserId>("user_id") {
        user_id
    } else {
        warn!("No session cookie");
        return HttpResponse::new(StatusCode::UNAUTHORIZED).into();
    };
    info!("cool session cooke, userid: {user_id:?}");
    return HttpResponse::Ok().json(build_feeds(user_id, services));
}

fn build_feeds(user: UserId, services: Data<Services>) -> Vec<FeedWithoutItem> {
    let feeds = services
        .subscriptions_service
        .lock()
        .unwrap()
        .get_feeds(&user);
    let feed_service = services.feed_service.lock().unwrap();
    feeds
        .into_iter()
        .map(|feed_id| {
            let feed = feed_service.feed(&feed_id).unwrap(); //todo not unwrap
            FeedWithoutItem {
                id: feed_id,
                name: feed.name,
                number_of_available_items: 0,
            }
        })
        .collect()
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

    let user_id = if let Ok(Some(user_id)) = session.get::<UserId>("user_id") {
        user_id
    } else {
        warn!("No session cookie");
        return HttpResponse::new(StatusCode::UNAUTHORIZED).into();
    };
    let url = FeedUrl(json.as_str().unwrap().to_string());
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
