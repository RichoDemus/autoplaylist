package com.richo.reader.test;

import com.richo.reader.test.pages.FeedPage;
import com.richo.reader.test.pages.LoginPage;
import com.richo.reader.test.pages.model.FeedId;
import com.richo.reader.test.pages.model.FeedUrl;
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

public class DownloadingTestIT
{
	private static final FeedId FEED_ID = new FeedId("UCyPvQQ-dZmKzh_PrpWmTJkw");
	private static final FeedUrl FEED_URL = new FeedUrl("https://www.youtube.com/user/richodemus");
	private TestableApplication target;
	private TestableApplication youtubeMock;
	private LoginPage loginPage;

	@Before
	public void setUp() throws Exception
	{
		youtubeMock = new TestableApplicationProvider().youtubeMock();
		target = new TestableApplicationProvider().readerApplication(youtubeMock.getHttpPort());
		loginPage = new LoginPage(target.getHttpPort());
	}

	@After
	public void tearDown() throws Exception
	{
		target.close();
		youtubeMock.close();
	}

	@Test
	public void newlyAddedFeedShouldNotContainItems() throws Exception
	{
		final String username = "richodemus";

		loginPage.createUser(username);
		loginPage.login(username);
		final FeedPage feedPage = loginPage.toFeedPage();
		feedPage.addFeed(FEED_URL);

		final List<String> result = feedPage.getItemNames(FEED_ID);

		assertThat(result).hasSize(0);
	}

	@Test
	public void shouldDownloadItemsAndAddThemToList() throws Exception
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
		assertThat(feedPage.getAllFeeds()).extracting("numberOfAvailableItems").containsExactly(2);
	}
}
