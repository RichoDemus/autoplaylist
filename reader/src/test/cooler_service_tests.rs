use anyhow::Result;
    use std::cell::OnceCell;
    use std::sync::{Arc, mpsc, Mutex, OnceLock};
    use std::sync::atomic::AtomicU16;
use std::time::{Duration, Instant};
use actix_test::TestServer;
    use actix_web::App;
    use super::*;
    use actix_session::SessionMiddleware;
    use actix_session::storage::CookieSessionStore;
    use actix_web::cookie::Key;
    use actix_cors::Cors;
    use actix_web::web;
    use actix_web::HttpResponse;

use crate::login;
    use crate::create_user;
    use crate::test::service::TestService;

// #[actix_rt::test]
// // #[tokio::test(flavor = "multi_thread")]
// async fn test_example2() {
//     let service = TestService::new();
//
//     let port = service.service.addr().port();
//
//     let response = reqwest::get(format!("http://localhost:{port}/")).await.unwrap();
//
//
//     // let req = srv.get("/");
//     // let res = req.send().await.unwrap();
//
//     println!("{:?}", response);
//     assert!(response.status().is_success());
// }

#[actix_rt::test]
async fn login_should_fail_if_no_user_exists() {
    let service = TestService::new();

    let client = service.client();

    let result = client.login().await;
    assert_eq!("Failed to log in", result.unwrap_err().to_string());
}

    #[actix_rt::test]
    async fn should_create_user_and_login() {
        let service = TestService::new();

        let client = service.client();

        let _result = client.create_user().await.unwrap();
        let _main_page = client.login().await;
    }

#[actix_rt::test]
async fn downloaded_feeds_should_be_in_feed_response() {
    let service = TestService::new();

    let client = service.client();

    let _result = client.create_user().await.unwrap();
    let main_page = client.login().await.unwrap();

    // no feeds
    assert!(main_page.get_feeds().await.unwrap().is_empty());

    main_page.add_feed("https://www.youtube.com/user/richodemus").await.unwrap();
    main_page.download_feeds().await.unwrap();

    let instant = Instant::now();
    let two_seconds = Duration::from_secs(2);
    // while (main_page.get_feeds().await.unwrap().is_empty() && Instant::now() - instant < two_seconds) {
    //     actix_rt::time::sleep(Duration::from_millis(10)).await;
    // }


    let feeds = main_page.get_feeds().await.unwrap();
    println!("{:?}", feeds);
    // assert_eq!(feeds.len(), 1, "Should be subscribed to one feed");
}

/*
    @Test
    internal fun `Downloaded items should be in feed response`() {
        loginPage.createUser()
        loginPage.login()

        val feedPage = loginPage.toFeedPage()
        feedPage.addFeed(FeedUrl("https://www.youtube.com/user/richodemus"))

        loginPage.downloadFeeds()

        await().atMost(1, TimeUnit.MINUTES).untilAsserted {
            assertThat(feedPage.getItemNames(FeedId("UCyPvQQ-dZmKzh_PrpWmTJkw"))).isNotEmpty()
        }


        assertThat(feedPage.getItemNames(FeedId("UCyPvQQ-dZmKzh_PrpWmTJkw"))).containsExactly("Zs6bAFlcH0M", "vtuDTx1oJGA")
        assertThat(feedPage.allFeeds).extracting("numberOfAvailableItems").containsExactly(2)
    }
 */