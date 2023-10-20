use crate::test::service::TestService;
use crate::test::test_client::MainPage;
use crate::test::test_utis::expected_videos;
use crate::test::test_utis::{await_videos, expected_videos2};
use crate::types::{ChannelId, Video, VideoId};
use itertools::assert_equal;
use log::info;
use log::trace;
use pretty_assertions::{assert_eq, assert_ne};
use std::future::Future;
use std::time::{Duration, Instant};

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
    let _main_page = client.login().await.unwrap();
}

#[actix_rt::test]
async fn login_should_fail_if_wrong_password() {
    let service = TestService::new();

    let client = service.client();

    let _result = client.create_user().await.unwrap();
    let _ = client.login_wrong_password().await.unwrap();
}

#[actix_rt::test]
async fn username_should_be_case_insensitive() {
    let service = TestService::new();

    let client = service.client();

    let _result = client.create_user().await.unwrap();
    let _main_page = client.login_upper_case_username().await.unwrap();
}

#[actix_rt::test]
async fn downloaded_feeds_should_be_in_feed_response() {
    let service = TestService::new();

    let client = service.client();

    let _result = client.create_user().await.unwrap();
    let main_page = client.login().await.unwrap();

    // no feeds
    assert!(main_page.get_feeds().await.unwrap().is_empty());

    main_page
        .add_feed("https://www.youtube.com/user/richodemus")
        .await
        .unwrap();
    actix_rt::time::sleep(Duration::from_millis(200)).await;
    main_page.download_feeds().await.unwrap();

    let feeds = main_page.get_feeds().await.unwrap();
    assert_eq!(feeds.len(), 1, "Should be subscribed to one feed");
    let feed = feeds.get(0).unwrap();
    assert_eq!(*feed.id, "richo-channel-id");
    assert_eq!(*feed.name, "richo-channel-name");

    await_videos(&main_page, feed.id.clone(), 4).await;
    let videos = main_page.get_videos(feed.id.clone()).await.unwrap();

    assert_eq!(videos, expected_videos());
}

#[actix_rt::test]
async fn should_not_continue_downoading_once_caught_up() {
    let mut service = TestService::new();

    let client = service.client();

    let _result = client.create_user().await.unwrap();
    let main_page = client.login().await.unwrap();

    // no feeds
    assert!(main_page.get_feeds().await.unwrap().is_empty());

    main_page
        .add_feed("https://www.youtube.com/user/richodemus")
        .await
        .unwrap();
    actix_rt::time::sleep(Duration::from_millis(200)).await;
    main_page.download_feeds().await.unwrap();

    let feeds = main_page.get_feeds().await.unwrap();
    assert_eq!(feeds.len(), 1, "Should be subscribed to one feed");
    let feed = feeds.get(0).unwrap();
    assert_eq!(*feed.id, "richo-channel-id");
    assert_eq!(*feed.name, "richo-channel-name");

    await_videos(&main_page, feed.id.clone(), 4).await;
    //videos downloaded, add new and download again
    actix_rt::time::sleep(Duration::from_millis(200)).await;
    main_page.download_feeds().await.unwrap();
    await_videos(&main_page, feed.id.clone(), 5).await;
    let videos = main_page.get_videos(feed.id.clone()).await.unwrap();
    trace!("new videos: {:?}", videos);
    assert_eq!(videos, expected_videos2());
}

#[actix_rt::test]
async fn should_not_contain_item_marked_as_read() {}

#[actix_rt::test]
async fn should_mark_item_as_unread() {}

#[actix_rt::test]
async fn create_label() {}

#[actix_rt::test]
async fn should_add_feed_to_label() {}
