#[cfg(test)]
mod tests {
    use std::cell::OnceCell;
    use std::sync::{Arc, Mutex, OnceLock};
    use actix_test::TestServer;
    use actix_web::App;
    use super::*;
    use actix_session::SessionMiddleware;
    use actix_session::storage::CookieSessionStore;
    use actix_web::cookie::Key;
    use std::time::Duration;
    use actix_cors::Cors;

    use crate::login;
    use crate::create_user;

    #[actix_rt::test]
    async fn test_example() {
        let secret_key = Key::from(&[0; 64]);
        let service = actix_test::start(||
                App::new()
                    .wrap(
                        SessionMiddleware::builder(CookieSessionStore::default(), secret_key.clone())
                            .cookie_secure(false)
                            .session_lifecycle(
                                actix_session::config::PersistentSession::default()
                                    .session_ttl(Duration::days(365)),
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
            );

        let port = service.addr().port();

        let response = reqwest::get(format!("http://localhost:{port}/")).await.unwrap();


        // let req = srv.get("/");
        // let res = req.send().await.unwrap();

        assert!(response.status().is_success());
        println!("{:?}", response);
    }
}