use actix_session::Session;
use actix_web::http::StatusCode;
use actix_web::{post, web, HttpResponse, Responder};
use actix_web::web::Bytes;
use aws_sdk_s3::model::Protocol::Http;
use log::{info, warn};
use serde_json::Value;
use uuid::uuid;

use crate::projections::user_service;
use crate::service::Services;
use crate::types::{Password, UserId, Username};

#[post("/v1/users")]
pub async fn create_user(json: web::Json<Value>, services: web::Data<Services>) -> HttpResponse {
    #[cfg(not(test))]
    return HttpResponse::Forbidden().into();
    let username = json["username"].as_str().unwrap();
    let password = json["password"].as_str().unwrap();

    let username = Username(username.to_string());
    let password = Password(password.to_string());
    info!("create user: {:?}. {username:?}/{password:?}", json);

    match services.user_service.lock().unwrap().create_user(username, password) {
        Ok(()) => HttpResponse::Ok().into(),
        Err(e) => {
            warn!("Failed to create user: {:?}", e);
            HttpResponse::InternalServerError().into()
        }
    }
}

// curl -v -X POST --header "Content-Type: application/json" --data "{\"username\":\"user\"}" localhost:8080/v1/sessions
#[post("/v1/sessions")]
pub async fn login(session: Session, body:Bytes, services: web::Data<Services>) -> HttpResponse {
    let body = String::from_utf8(body.to_vec()).unwrap();
    info!("login: {}", body);
    let json:Value = serde_json::from_str(body.as_str()).unwrap();
    info!("json: {:?}", json);
    let username = json["username"].as_str().unwrap();
    let password = json["password"].as_str().unwrap();
    let username = Username(username.to_string());
    let password = Password(password.to_string());

    if services.user_service.lock().unwrap().is_password_valid(&username, &password) {
        if let Some(user_id) = services.user_service.lock().unwrap().get_user_id(&username) {
            info!("logged in {username:?} ({user_id:?}");
                session.insert("username", username).unwrap();
                session.insert("user_id", user_id).unwrap();
                return HttpResponse::Ok().into()
        }
    }
    HttpResponse::new(StatusCode::UNAUTHORIZED).into()
    // session.insert("username", &json["username"]).unwrap();
    // session.insert("user_id", UserId(uuid!("00000000-0000-0000-0000-000000000000"))).unwrap();
    // HttpResponse::Ok().into()
    // let username = json["username"].as_str().unwrap();
    // let password = json["password"].as_str().unwrap();
    // info!("body: {:?}. {username}/{password}", json);
    // let username = Username(username.to_string());
    // let password = Password(password.to_string());
    // let user_exists = user_service::is_password_valid(&username, &password);
    // info!("User {username:?} exists: {user_exists}");
    // if user_exists {
    //     let maybe_user_id = user_service::get_user_id(&username);
    //     let user_id = maybe_user_id.unwrap();
    //     info!("logged in {username:?} ({user_id:?}");
    //     session.insert("username", username).unwrap();
    //     session.insert("user_id", user_id).unwrap();
    //     HttpResponse::Ok().into()
    // } else {
    //     HttpResponse::new(StatusCode::UNAUTHORIZED).into()
    // }
}
