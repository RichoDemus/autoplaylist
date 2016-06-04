package com.richo.reader.test;

import com.google.common.collect.Sets;
import com.jayway.restassured.RestAssured;
import com.richo.reader.test.pages.FeedPage;
import com.richo.reader.test.pages.LoginPage;
import com.richo.reader.test.util.DropwizardContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.jayway.restassured.RestAssured.post;
import static org.assertj.core.api.Assertions.assertThat;

public class FeedTestIT
{
	private DropwizardContainer target;
	private DropwizardContainer youtubeMock;
	private String baseUrl;
	private LoginPage loginPage;

	@Before
	public void setUp() throws Exception
	{
		youtubeMock = new DropwizardContainer("richodemus/youtubemock:latest");
		final String youtubeIp = youtubeMock.getIp();
		final int port = 8080;
		target = new DropwizardContainer("richodemus/reader", Sets.newHashSet("YOUTUBE_URL=http://" + youtubeIp + ":" + port + "/"));
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

		final List<String> result = feedPage.getAllFeedNames();

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
	public void getFeedsShouldContainAddedFeed() throws Exception
	{
		final String username = "richodemus";
		final String feedName = "richodemus";
		loginPage.createUser(username);
		loginPage.login(username);
		final FeedPage feedPage = loginPage.toFeedPage();

		feedPage.addFeed(feedName);

		final List<String> result = feedPage.getAllFeedNames();
		assertThat(result).containsExactly(feedName);
	}


	@Test(timeout = 30_000L)
	public void shouldNotContainItemMarkedAsRead() throws Exception
	{
		final String username = "richodemus";
		final String feedName = "richodemus";

		loginPage.createUser(username);
		loginPage.login(username);
		final FeedPage feedPage = loginPage.toFeedPage();
		feedPage.addFeed(feedName);


		final int adminPort = target.getAdminPort();
		post("http://localhost:" + adminPort + "/tasks/download").then().statusCode(200);

		while (feedPage.getItemNames(feedName).size() == 0)
		{
			Thread.sleep(100L);
		}

		assertThat(feedPage.getItemNames(feedName)).containsExactly("Zs6bAFlcH0M", "vtuDTx1oJGA");

		feedPage.markAsRead(feedName, "vtuDTx1oJGA");

		assertThat(feedPage.getItemNames(feedName)).containsExactly("Zs6bAFlcH0M");
	}
}
