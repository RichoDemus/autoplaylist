use crate::test::service::TestService;
use itertools::assert_equal;

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
    main_page.download_feeds().await.unwrap();

    let feeds = main_page.get_feeds().await.unwrap();
    println!("got feed: {:?}", feeds);
    assert_eq!(feeds.len(), 1, "Should be subscribed to one feed");
    let feed = feeds.get(0).unwrap();
    assert_eq!(*feed.id, "richo-channel-id");
    assert_eq!(*feed.name, "richo-channel-name");

    let items = main_page.get_feed(feed.id.clone()).await.unwrap();
    println!("items: {:?}", items);
}
