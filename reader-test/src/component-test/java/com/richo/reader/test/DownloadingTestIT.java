package com.richo.reader.test;

import com.google.common.collect.Sets;
import com.richo.reader.test.pages.FeedPage;
import com.richo.reader.test.pages.LoginPage;
import com.richo.reader.test.pages.model.FeedId;
import com.richo.reader.test.util.DropwizardContainer;
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
	private static final FeedId FEED_ID = new FeedId("richodemus");
	private DropwizardContainer target;
	private DropwizardContainer youtubeMock;
	private LoginPage loginPage;

	@Before
	public void setUp() throws Exception
	{
		youtubeMock = new DropwizardContainer("richodemus/youtubemock:latest");
		final String youtubeIp = youtubeMock.getIp();
		final int port = 8080;
		target = new DropwizardContainer("richodemus/reader", Sets.newHashSet("YOUTUBE_URL=http://" + youtubeIp + ":" + port + "/"));
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
		feedPage.addFeed(FEED_ID);

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
		feedPage.addFeed(FEED_ID);


		final int adminPort = target.getAdminPort();
		post("http://localhost:" + adminPort + "/tasks/download").then().statusCode(200);

		await().atMost(1, MINUTES).until(() -> assertThat(feedPage.getItemNames(FEED_ID)).isNotEmpty());

		assertThat(feedPage.getItemNames(FEED_ID)).containsExactly("Zs6bAFlcH0M", "vtuDTx1oJGA");
		assertThat(feedPage.getAllFeeds()).extracting("numberOfAvailableItems").containsExactly(2);
	}
}
