package com.richo.reader.test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.richo.reader.test.util.DropwizardContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FeedTestIT
{
	private DropwizardContainer container;

	@Before
	public void setUp() throws Exception
	{
		container = new DropwizardContainer("richodemus/reader");
	}

	@After
	public void tearDown() throws Exception
	{
		container.close();
	}

	@Test
	public void shouldReturnZeroFeedsIfNoneAreDownloaded() throws Exception
	{
		RestAssured
				.given().body("richodemus")
				.when().post("http://localhost:8080/api/users")
				.then().assertThat().statusCode(200);

		final String token = RestAssured
				.given().body("123456789qwertyuio123qweasd")
				.when().post("http://localhost:8080/api/users/richodemus/sessions")
				.then().assertThat().statusCode(200).extract().body().jsonPath().get("token");

		final List<String> feeds = RestAssured
				.given().header("x-token-jwt", token)
				.when().get("http://localhost:8080/api/users/richodemus/feeds/")
				.then().assertThat().statusCode(200).extract().body().jsonPath().get("feeds");

		assertThat(feeds).hasSize(0);
	}

	@Test
	public void shouldNotReturnFeedsWithoutToken() throws Exception
	{
		RestAssured
				.given()
				.when().get("http://localhost:8080/api/users/richodemus/feeds/")
				.then().assertThat().statusCode(403);
	}

	@Test
	public void getFeedsShouldContainAddedFeed() throws Exception
	{
		final String feedName = "richodemus";

		RestAssured
				.given().body("richodemus")
				.when().post("http://localhost:8080/api/users")
				.then().assertThat().statusCode(200);

		final String token = RestAssured
				.given().body("123456789qwertyuio123qweasd")
				.when().post("http://localhost:8080/api/users/richodemus/sessions")
				.then().assertThat().statusCode(200).extract().body().jsonPath().get("token");

		RestAssured
				.given().header("x-token-jwt", token).body(feedName).contentType(ContentType.JSON)
				.when().post("http://localhost:8080/api/users/richodemus/feeds/")
				.then().assertThat().statusCode(204);

		final List<String> feeds = RestAssured
				.given().header("x-token-jwt", token)
				.when().get("http://localhost:8080/api/users/richodemus/feeds/")
				.then().assertThat().statusCode(200).extract().body().jsonPath().get("feeds.name");

		assertThat(feeds).containsExactly(feedName);
	}
}
