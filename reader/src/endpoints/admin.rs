use actix_http::StatusCode;
use actix_session::Session;
use actix_web::{HttpResponse, post};
use log::{info, warn};
use crate::types::UserId;

#[post("/admin/download")]
pub async fn download(session: Session) -> HttpResponse {

    info!("Session status: {:?}", session.status());
    info!("Session entries: {:?}", session.entries());

    let user_id = if let Ok(Some(user_id)) = session.get::<UserId>("user_id") {
        user_id
    } else {
        warn!("No session cookie");
        return HttpResponse::new(StatusCode::UNAUTHORIZED).into();
    };
    info!("cool session cooke, userid: {user_id:?}");
    info!("Downloading feeds");
    return HttpResponse::Ok().into()
}