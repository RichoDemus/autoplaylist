use actix_http::body::{BoxBody, EitherBody, MessageBody};
use actix_web::dev::{Service, ServiceResponse};
use actix_web::test;
use anyhow::{bail, Result};
use serde_json::{json, Value};

pub struct TestClient {}

impl TestClient {
    pub async fn create_user<T>(&self, app: &impl actix_web::dev::Service<T, Response=ServiceResponse<EitherBody<BoxBody>>, Error=actix_web::Error>, username: &str, password: &str) -> Result<()> {
        let req = test::TestRequest::post()
            .uri("/v1/sessions")
            .set_json(json!({"username":username, "password":password}))
            .to_request();

        let resp = test::call_service(&app, req).await;
        println!("{:?}", resp.status());
        assert!(resp.status().is_success());
        let mut body = resp.into_body();
        let b = body.try_into_bytes();
        let b = b.expect("asd");
        let v: Value = serde_json::from_slice(&b).unwrap();
        println!("{:?}", v);
        Ok(())
    }
}