use actix_http::StatusCode;
use actix_session::Session;
use actix_web::{get, HttpResponse, post};
use actix_web::web::Json;
use log::{info, warn};
use serde_json::{json, Value};
use crate::types::UserId;

#[get("/v1/feeds")]
pub async fn get_all_feeds(session: Session) -> HttpResponse {
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
    return HttpResponse::Ok().json(json!([]))
}

#[post("/v1/feeds")]
pub async fn add_feed(session: Session, json: Json<Value>) -> HttpResponse {
    info!("add feed: {:?}", json);
    info!("Session status: {:?}", session.status());
    info!("Session entries: {:?}", session.entries());

    let user_id = if let Ok(Some(user_id)) = session.get::<UserId>("user_id") {
        user_id
    } else {
        warn!("No session cookie");
        return HttpResponse::new(StatusCode::UNAUTHORIZED).into();
    };
    info!("cool session cooke, userid: {user_id:?}");
    return HttpResponse::Ok().into()
}
