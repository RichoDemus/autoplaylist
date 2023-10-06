#[cfg(test)]
mod tests {

    use actix_cors::Cors;
    use actix_session::storage::CookieSessionStore;
    use actix_session::SessionMiddleware;
    use actix_web::cookie::Key;
    use actix_web::{cookie, test, App};
    use anyhow::Result;
    use serde_json::json;

    use crate::endpoints::user::{create_user, login};

    #[tokio::test(flavor = "multi_thread")]
    async fn login_should_fail_if_user_doesnt_exist() -> Result<()> {
        std::env::set_var("RUST_LOG", "info");
        let _ = env_logger::try_init();
        let secret_key = Key::from(&[0; 64]);
        let app = test::init_service(
            App::new()
                .wrap(
                    SessionMiddleware::builder(CookieSessionStore::default(), secret_key.clone())
                        .cookie_secure(false)
                        .session_lifecycle(
                            actix_session::config::PersistentSession::default()
                                .session_ttl(cookie::time::Duration::days(365)),
                        )
                        .build(),
                )
                .wrap(
                    Cors::default()
                        .supports_credentials()
                        .allow_any_origin()
                        .allow_any_method()
                        .allow_any_header(),
                )
                .service(login)
                .service(create_user),
        )
        .await;

        // login before create user, should fail
        {
            let req = test::TestRequest::post()
                .uri("/v1/sessions")
                .set_json(json!({"username":"cool-user", "password":"a-password"}))
                .to_request();
            let resp = test::call_service(&app, req).await;
            println!("headers: {:?}", resp.headers());
            assert!(!resp.status().is_success())
        }

        //  create user
        {
            let req = test::TestRequest::post()
                .uri("/v1/users")
                .set_json(
                    json!({"username":"cool-user", "password":"a-password", "inviteCode":"icode"}),
                )
                .to_request();
            let resp = test::call_service(&app, req).await;
            assert!(resp.status().is_success());
        }

        // login
        {
            let req = test::TestRequest::post()
                .uri("/v1/sessions")
                .set_json(json!({"username":"cool-user", "password":"a-password"}))
                .to_request();
            let resp = test::call_service(&app, req).await;
            println!("headers: {:?}", resp.headers());
            assert!(resp.status().is_success());
            assert!(resp.headers().get("set-cookie").is_some())
        }

        Ok(())
    }
}
