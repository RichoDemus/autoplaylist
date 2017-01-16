package com.richo.reader.test;

import com.google.common.collect.Sets;
import com.richo.reader.test.pages.FeedPage;
import com.richo.reader.test.pages.LoginPage;
import com.richo.reader.test.pages.model.FeedId;
import com.richo.reader.test.pages.model.Label;
import com.richo.reader.test.util.TestableApplication;
import com.richo.reader.test.util.DropwizardContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.jayway.restassured.RestAssured.post;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class LabelTestIT
{
	private static final FeedId FEED_ID = new FeedId("richodemus");
	private TestableApplication target;
	private TestableApplication youtubeMock;
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
	public void shouldCreateLabel() throws Exception
	{
		final String username = "richodemus";
		final String labelName = "my-label";

		loginPage.createUser(username);
		loginPage.login(username);
		final FeedPage feedPage = loginPage.toFeedPage();

		final int id = feedPage.createLabel(labelName);

		assertThat(id).isEqualTo(0);
	}

	@Test
	public void shouldAddFeedToLabel() throws Exception
	{
		final String username = "richodemus";
		final String labelName = "my-label";

		loginPage.createUser(username);
		loginPage.login(username);
		final FeedPage feedPage = loginPage.toFeedPage();
		feedPage.addFeed(FEED_ID);


		final int adminPort = target.getAdminPort();
		post("http://localhost:" + adminPort + "/tasks/download").then().statusCode(200);

		await().atMost(1, MINUTES).until(() -> assertThat(feedPage.getItemNames(FEED_ID)).isNotEmpty());

		final int labelId = feedPage.createLabel(labelName);
		feedPage.addFeedToLabel(FEED_ID, labelId);

		final List<Label> result = feedPage.getLabels();

		assertThat(result).flatExtracting("feeds").containsOnly(FEED_ID.toString());
	}
}
