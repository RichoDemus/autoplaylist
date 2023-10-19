use crate::endpoints::admin::download;
use crate::endpoints::feeds::{add_feed, get_all_feeds, get_videos};
use crate::endpoints::user::{create_user, login};
use crate::service::Services;
use crate::test::test_client::LoginPage;
use crate::test::youtube_mock::setup_youtube_mock;
use actix_cors::Cors;
use actix_session::storage::CookieSessionStore;
use actix_session::SessionMiddleware;
use actix_test::TestServer;
use actix_web::cookie::Key;
use actix_web::{web, App};
use httpmock::prelude::*;
use httpmock::MockServer;
use log::LevelFilter;
use serde_json::json;
use std::env;

pub struct TestService {
    pub service: TestServer,
    mock_server: MockServer,
}
impl TestService {
    pub fn new() -> Self {
        let _ = env_logger::builder()
            .filter_module("reader", LevelFilter::Trace)
            .try_init();

        let mock_server = MockServer::start();
        setup_youtube_mock(&mock_server);

        let secret_key = Key::from(&[0; 64]);
        let state = web::Data::new(Services::default());

        Self {
            service: actix_test::start(move || {
                App::new()
                    .app_data(state.clone())
                    .wrap(
                        SessionMiddleware::builder(
                            CookieSessionStore::default(),
                            secret_key.clone(),
                        )
                        .cookie_secure(false)
                        .session_lifecycle(
                            actix_session::config::PersistentSession::default()
                                .session_ttl(actix_web::cookie::time::Duration::days(365)),
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
                    .service(create_user)
                    .service(get_all_feeds)
                    .service(get_videos)
                    .service(add_feed)
                    .service(download)
            }),
            mock_server,
        }
    }

    pub fn client(&self) -> LoginPage {
        LoginPage::new(self.service.addr().port())
    }
}
