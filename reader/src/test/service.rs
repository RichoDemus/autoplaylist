use actix_cors::Cors;
use actix_session::storage::CookieSessionStore;
use actix_session::SessionMiddleware;
use actix_test::TestServer;
use actix_web::cookie::Key;
use actix_web::{web, App};
use log::LevelFilter;

use crate::endpoints::admin::{download, get_status};
use crate::endpoints::feeds::{add_feed, feed_operation, get_all_feeds, get_videos};
use crate::endpoints::labels::{add_video_to_label, create_label};
use crate::endpoints::user::{check_session, create_user, login};
use crate::service::Services;
use crate::sled_wrapper::Mode;
use crate::test::test_client::LoginPage;
use crate::test::youtube_mock::YoutubeMock;

pub struct TestService {
    pub service: TestServer,
    _youtube_mock: YoutubeMock,
}

impl TestService {
    pub fn new() -> Self {
        let _ = env_logger::builder()
            .filter_module("reader", LevelFilter::Trace)
            .try_init();

        let youtube_mock = YoutubeMock::default();

        let secret_key = Key::from(&[0; 64]);
        let state = web::Data::new(Services::new(
            Some(youtube_mock.base_url()),
            "YT_KEY".to_string(),
            Mode::Test,
            false,
            None,
        ));

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
                    .service(check_session)
                    .service(create_user)
                    .service(get_all_feeds)
                    .service(get_videos)
                    .service(add_feed)
                    .service(feed_operation)
                    .service(download)
                    .service(get_status)
                    .service(create_label)
                    .service(add_video_to_label)
            }),
            _youtube_mock: youtube_mock,
        }
    }

    pub fn client(&self) -> LoginPage {
        LoginPage::new(self.service.addr().port())
    }
}
