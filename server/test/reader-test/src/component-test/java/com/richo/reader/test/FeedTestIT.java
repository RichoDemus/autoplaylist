package com.richo.reader.test;

import com.jayway.restassured.RestAssured;
import com.richo.reader.test.pages.FeedPage;
import com.richo.reader.test.pages.LoginPage;
import com.richo.reader.test.pages.model.FeedId;
import com.richo.reader.test.pages.model.FeedUrl;
import com.richo.reader.test.pages.model.FeedWithoutItem;
import com.richo.reader.test.util.TestableApplication;
import com.richo.reader.test.util.TestableApplicationProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.jayway.restassured.RestAssured.post;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class FeedTestIT
{
	private static final FeedId FEED_ID = new FeedId("UCyPvQQ-dZmKzh_PrpWmTJkw");
	private static final FeedUrl FEED_URL = new FeedUrl("https://www.youtube.com/user/richodemus");
	private TestableApplication target;
	private TestableApplication youtubeMock;
	private String baseUrl;
	private LoginPage loginPage;

	@Before
	public void setUp() throws Exception
	{
		youtubeMock = new TestableApplicationProvider().youtubeMock();
		target = new TestableApplicationProvider().readerApplication(youtubeMock.getHttpPort());
		baseUrl = "http://localhost:" + target.getHttpPort();
		loginPage = new LoginPage(target.getHttpPort());
	}

	@After
	public void tearDown() throws Exception
	{
		target.close();
		youtubeMock.close();
	}

	@Test
	public void shouldReturnZeroFeedsIfNoneAreDownloaded() throws Exception
	{
		final String username = "richodemus";
		loginPage.createUser(username);
		loginPage.login(username);
		final FeedPage feedPage = loginPage.toFeedPage();

		final List<FeedWithoutItem> result = feedPage.getAllFeeds();

		assertThat(result).hasSize(0);
	}

	@Test
	public void shouldNotReturnFeedsWithoutToken() throws Exception
	{
		RestAssured
				.given()
				.when().get(baseUrl + "/api/users/richodemus/feeds/")
				.then().assertThat().statusCode(403);
	}

	@Test
	public void newlyAddedFieldShouldHaveSpecialName() throws Exception
	{
		final String username = "richodemus";
		loginPage.createUser(username);
		loginPage.login(username);
		final FeedPage feedPage = loginPage.toFeedPage();

		feedPage.addFeed(FEED_URL);

		final List<FeedWithoutItem> result = feedPage.getAllFeeds();
		assertThat(result).extracting("name").containsExactly("UNKNOWN_FEED");
	}


	@Test
	public void shouldNotContainItemMarkedAsRead() throws Exception
	{
		final String username = "richodemus";

		loginPage.createUser(username);
		loginPage.login(username);
		final FeedPage feedPage = loginPage.toFeedPage();
		feedPage.addFeed(FEED_URL);


		final int adminPort = target.getAdminPort();
		post("http://localhost:" + adminPort + "/tasks/download").then().statusCode(200);

		await().atMost(1, MINUTES).untilAsserted(() -> assertThat(feedPage.getItemNames(FEED_ID)).isNotEmpty());

		assertThat(feedPage.getItemNames(FEED_ID)).containsExactly("Zs6bAFlcH0M", "vtuDTx1oJGA");

		feedPage.markAsRead(FEED_ID, "vtuDTx1oJGA");

		assertThat(feedPage.getItemNames(FEED_ID)).containsExactly("Zs6bAFlcH0M");
		assertThat(feedPage.getAllFeeds()).extracting("numberOfAvailableItems").containsExactly(1);
	}
}
