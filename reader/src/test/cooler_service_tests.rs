use crate::test::service::TestService;

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

    // let instant = Instant::now();
    // let two_seconds = Duration::from_secs(2);
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
