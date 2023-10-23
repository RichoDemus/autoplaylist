use actix_http::StatusCode;
use actix_session::Session;
use actix_web::web::Bytes;
use actix_web::web::{Data, Json, Path};
use actix_web::{get, post, HttpResponse};
use log::{info, warn};
use serde_json::Value;

use crate::endpoints::endpoint_types::{AllFeedsAndLabelsResponse, Operation};
use crate::service::Services;
use crate::types::{ChannelId, ChannelWithoutVideos, UserId, Video, VideoId, YoutubeChannelUrl};

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
    let labels = services.label_service.lock().unwrap().get_labels(&user_id);
    let channels = build_feeds(user_id, services);

    return HttpResponse::Ok().json(AllFeedsAndLabelsResponse {
        feeds: channels,
        labels,
    });
}

fn build_feeds(user: UserId, services: Data<Services>) -> Vec<ChannelWithoutVideos> {
    let feeds = services
        .subscriptions_service
        .lock()
        .unwrap()
        .get_feeds(&user);
    let feed_service = services.feed_service.lock().unwrap();
    feeds
        .into_iter()
        .flat_map(|feed_id| feed_service.channel(feed_id))
        .map(|feed| ChannelWithoutVideos {
            id: feed.id,
            name: feed.name,
            number_of_available_items: 0,
        })
        .collect()
}

#[get("/v1/feeds/{id}/items")]
pub async fn get_videos(
    session: Session,
    services: Data<Services>,
    feed_id: Path<ChannelId>,
) -> HttpResponse {
    info!("get videos {feed_id:?}");
    let user_id = if let Ok(user_id) = session.try_into() {
        user_id
    } else {
        warn!("No session cookie");
        return HttpResponse::new(StatusCode::UNAUTHORIZED).into();
    };
    let channel_id: ChannelId = feed_id.into_inner();
    let watched_videos = services
        .watched_videos_service
        .lock()
        .unwrap()
        .watched_items(&user_id, &channel_id);
    let mut feed: Vec<Video> = services.feed_service.lock().unwrap().videos(channel_id);
    feed.retain(|video| !watched_videos.contains(&video.id));

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
        .await
        .unwrap();
    return HttpResponse::Ok().into();
}

#[post("/v1/feeds/{feed}/items/{item}")]
pub async fn feed_operation(
    session: Session,
    services: Data<Services>,
    feed_id: Path<(ChannelId, VideoId)>,
    body: Bytes,
) -> HttpResponse {
    let body = String::from_utf8(body.to_vec()).unwrap();
    info!("login: {}", body);
    let mut json: Value = serde_json::from_str(body.as_str()).unwrap();
    info!("json: {:?}", json);

    let operation: Operation = serde_json::from_value(json["action"].take()).unwrap();

    info!("Feed operation: {:?}  {:?}", feed_id, operation);

    let user_id = if let Ok(user_id) = session.try_into() {
        user_id
    } else {
        warn!("No session cookie");
        return HttpResponse::new(StatusCode::UNAUTHORIZED).into();
    };

    let (channel_id, video_id) = feed_id.into_inner();
    match operation {
        Operation::MarkRead => services
            .watched_videos_service
            .lock()
            .unwrap()
            .watch_item(user_id, channel_id, video_id)
            .await
            .unwrap(),
        Operation::MarkUnread => services
            .watched_videos_service
            .lock()
            .unwrap()
            .unwatch_item(user_id, channel_id, video_id)
            .await
            .unwrap(),
    }

    HttpResponse::Ok().into()
}
