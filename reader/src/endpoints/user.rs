use actix_session::Session;
use actix_web::http::StatusCode;
use actix_web::{post, web, HttpResponse, Responder};
use log::info;
use serde_json::Value;

use crate::projections::user_service;
use crate::types::{Password, Username};

#[post("/v1/users")]
pub async fn create_user(json: web::Json<Value>) -> impl Responder {
    let username = json["username"].as_str().unwrap();
    let password = json["password"].as_str().unwrap();
    let code = json["inviteCode"].as_str().unwrap();
    info!("create user: {:?}. {username}/{password} - {code}", json);

    let username = Username(username.to_string());
    let password = Password(password.to_string());
    let _result = user_service::create_user(username, password).await.unwrap();

    HttpResponse::Ok()
}

#[post("/v1/sessions")]
pub async fn login(session: Session, json: web::Json<Value>) -> HttpResponse {
    let username = json["username"].as_str().unwrap();
    let password = json["password"].as_str().unwrap();
    info!("body: {:?}. {username}/{password}", json);
    let username = Username(username.to_string());
    let password = Password(password.to_string());
    let user_exists = user_service::is_password_valid(&username, &password);
    if user_exists {
        session.insert("key", "value").unwrap();
        HttpResponse::Ok().into()
    } else {
        HttpResponse::new(StatusCode::UNAUTHORIZED).into()
    }
}
