use crate::service::Services;
use crate::types::UserId;
use actix_http::StatusCode;
use actix_session::Session;
use actix_web::web::Data;
use actix_web::{post, HttpResponse};
use log::{error, info, warn};

#[post("/admin/download")]
pub async fn download(session: Session, services: Data<Services>) -> HttpResponse {
    info!("Session status: {:?}", session.status());
    info!("Session entries: {:?}", session.entries());

    let user_id: UserId = if let Ok(user_id) = session.try_into() {
        user_id
    } else {
        warn!("No session cookie");
        return HttpResponse::new(StatusCode::UNAUTHORIZED).into();
    };
    info!("cool session cooke, userid: {user_id:?}");
    info!("Downloading feeds");
    if let Err(e) = services.feed_service.lock().unwrap().download().await {
        error!("Failed to download: {e:?}");
        return HttpResponse::InternalServerError().into();
    }
    return HttpResponse::Ok().into();
}
