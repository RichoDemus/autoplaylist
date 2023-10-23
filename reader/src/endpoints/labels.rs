use actix_http::StatusCode;
use actix_session::Session;
use actix_web::web::{Bytes, Data, Path};
use actix_web::{post, HttpResponse};
use log::{trace, warn};

use crate::service::Services;
use crate::types::{ChannelId, LabelId, LabelName, UserId};

#[post("/v1/labels")] // todo make sure no endpoint ends with /
pub async fn create_label(session: Session, body: Bytes, services: Data<Services>) -> HttpResponse {
    let body = String::from_utf8(body.to_vec()).unwrap();
    let label_name: LabelName = serde_json::from_str(&body).unwrap();
    // let label_name = LabelName(body);
    trace!("Create label: {label_name:?}");

    let user_id = if let Ok(user_id) = session.try_into() {
        user_id
    } else {
        warn!("No session cookie");
        return HttpResponse::new(StatusCode::UNAUTHORIZED);
    };

    services
        .label_service
        .lock()
        .unwrap()
        .create_label(user_id, label_name)
        .await
        .unwrap();

    HttpResponse::Ok().into()
}

#[post("/v1/labels/{id}")]
pub async fn add_video_to_label(
    session: Session,
    body: Bytes,
    services: Data<Services>,
    label_id: Path<LabelId>,
) -> HttpResponse {
    let body = String::from_utf8(body.to_vec()).unwrap();
    let channel: ChannelId = serde_json::from_str(&body).unwrap();
    trace!("Add channel {:?} to label {:?}", channel, label_id);

    let user_id = if let Ok(user_id) = session.try_into() {
        user_id
    } else {
        warn!("No session cookie");
        return HttpResponse::new(StatusCode::UNAUTHORIZED);
    };
    let _user_id: UserId = user_id;
    // todo user_id is not checked, label should be owned by user
    services
        .label_service
        .lock()
        .unwrap()
        .add_channel_to_label(channel, label_id.into_inner())
        .await
        .unwrap();
    HttpResponse::Ok().into()
}
