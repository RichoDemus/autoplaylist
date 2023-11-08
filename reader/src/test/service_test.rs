use crate::test::service::TestService;
use crate::test::test_utils::await_videos;
use crate::test::test_utils::expected_videos;
use crate::test::test_utils::expected_videos2;
use crate::test::test_utils::expected_videos_read;
use crate::types::ChannelId;
use crate::types::VideoId;
use log::trace;
use std::time::Duration;

#[actix_rt::test]
async fn login_should_fail_if_no_user_exists() {
    let service = TestService::new();

    let mut client = service.client();

    let result = client.login().await;
    assert_eq!("Failed to log in", result.unwrap_err().to_string());
}

#[actix_rt::test]
async fn should_create_user_and_login() {
    let service = TestService::new();

    let mut client = service.client();

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
async fn session_endpoint_should_fail_when_not_logged_in() {
    let service = TestService::new();

    let client = service.client();

    let result = client.has_session().await;
    assert_eq!(result, false);
}

#[actix_rt::test]
async fn session_endpoint_should_succeed_when_logged_in() {
    let service = TestService::new();

    let mut client = service.client();

    let _result = client.create_user().await.unwrap();
    client.login().await.unwrap();

    let result = client.has_session().await;
    assert!(result);
}

#[actix_rt::test]
async fn downloaded_feeds_should_be_in_feed_response() {
    let service = TestService::new();

    let mut client = service.client();

    let _result = client.create_user().await.unwrap();
    client.login().await.unwrap();
    let main_page = client.main_page();

    // no feeds
    assert!(main_page.get_feeds().await.unwrap().feeds.is_empty());

    main_page
        .add_feed("https://www.youtube.com/user/richodemus")
        .await
        .unwrap();
    actix_rt::time::sleep(Duration::from_millis(200)).await;
    main_page.download_feeds().await.unwrap();

    let feeds = main_page.get_feeds().await.unwrap().feeds;
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
    let service = TestService::new();

    let mut client = service.client();

    let _result = client.create_user().await.unwrap();
    client.login().await.unwrap();
    let main_page = client.main_page();

    // no feeds
    assert!(main_page.get_feeds().await.unwrap().feeds.is_empty());

    main_page
        .add_feed("https://www.youtube.com/user/richodemus")
        .await
        .unwrap();
    actix_rt::time::sleep(Duration::from_millis(200)).await;
    main_page.download_feeds().await.unwrap();

    let feeds = main_page.get_feeds().await.unwrap().feeds;
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
async fn should_not_contain_item_marked_as_read() {
    let service = TestService::new();

    let mut client = service.client();

    let _result = client.create_user().await.unwrap();
    client.login().await.unwrap();
    let main_page = client.main_page();

    // no feeds
    assert!(main_page.get_feeds().await.unwrap().feeds.is_empty());

    main_page
        .add_feed("https://www.youtube.com/user/richodemus")
        .await
        .unwrap();
    actix_rt::time::sleep(Duration::from_millis(200)).await;
    main_page.download_feeds().await.unwrap();

    let channels = main_page.get_feeds().await.unwrap().feeds;
    assert_eq!(channels.len(), 1, "Should be subscribed to one feed");
    let channel = channels.get(0).unwrap();
    assert_eq!(*channel.id, "richo-channel-id");
    assert_eq!(*channel.name, "richo-channel-name");

    await_videos(&main_page, channel.id.clone(), 4).await;
    let videos = main_page.get_videos(channel.id.clone()).await.unwrap();

    assert_eq!(videos, expected_videos());

    main_page
        .mark_as_read(channel.id.clone(), VideoId("video1-id".to_string()))
        .await
        .unwrap();
    main_page
        .mark_as_read(channel.id.clone(), VideoId("video2-id".to_string()))
        .await
        .unwrap();
    main_page
        .mark_as_read(channel.id.clone(), VideoId("video3-id".to_string()))
        .await
        .unwrap();
    main_page
        .mark_as_unread(channel.id.clone(), VideoId("video3-id".to_string()))
        .await
        .unwrap();
    actix_rt::time::sleep(Duration::from_millis(200)).await;
    let videos = main_page.get_videos(channel.id.clone()).await.unwrap();
    trace!("new videos: {:?}", videos);
    assert_eq!(videos, expected_videos_read());
}

#[actix_rt::test]
async fn should_not_be_any_labels() {
    // generic setup
    let service = TestService::new();
    let mut client = service.client();
    let _result = client.create_user().await.unwrap();
    client.login().await.unwrap();
    let main_page = client.main_page();
    main_page
        .add_feed("https://www.youtube.com/user/richodemus")
        .await
        .unwrap();

    // test
    let response = main_page.get_feeds().await.unwrap();
    assert!(response.labels.is_empty());
}

#[actix_rt::test]
async fn create_label() {
    // generic setup
    let service = TestService::new();
    let mut client = service.client();
    let _result = client.create_user().await.unwrap();
    client.login().await.unwrap();
    let main_page = client.main_page();
    main_page
        .add_feed("https://www.youtube.com/user/richodemus")
        .await
        .unwrap();

    // test
    main_page.create_label("label-name").await.unwrap();
    let response = main_page.get_feeds().await.unwrap();
    assert_eq!(response.labels.len(), 1);
    let label = response.labels[0].clone();
    assert_eq!(*label.name, "label-name");
    assert!(label.feeds.is_empty());
}

#[actix_rt::test]
async fn should_add_feed_to_label() {
    // generic setup
    let service = TestService::new();
    let mut client = service.client();
    let _result = client.create_user().await.unwrap();
    client.login().await.unwrap();
    let main_page = client.main_page();
    main_page
        .add_feed("https://www.youtube.com/user/richodemus")
        .await
        .unwrap();

    // test
    main_page.create_label("label-name").await.unwrap();
    let response = main_page.get_feeds().await.unwrap();
    let feed = response.feeds[0].id.clone();
    let label = response.labels[0].id.clone();
    main_page.add_feed_to_label(feed, label).await.unwrap();
    actix_rt::time::sleep(Duration::from_millis(200)).await;

    let response = main_page.get_feeds().await.unwrap();
    let channels_for_label = response.labels[0].feeds.clone();
    assert_eq!(
        channels_for_label,
        vec![ChannelId("richo-channel-id".to_string())]
    )
}
