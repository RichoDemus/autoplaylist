package com.richo.reader.test;

import com.google.common.collect.Sets;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.richo.reader.test.util.DropwizardContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.function.BooleanSupplier;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.post;

public class DownloadingTestIT
{
	private static final BooleanSupplier DROPWIZARD_CHECK = () -> get("http://localhost:80/").then().extract().statusCode() == 200;
	private DropwizardContainer target;
	private DropwizardContainer youtubeMock;
	private String baseUrl;

	@Before
	public void setUp() throws Exception
	{
		youtubeMock = new DropwizardContainer("richodemus/youtubemock:latest");
		final String youtubeIp = youtubeMock.getIp();
		final int port = 8080;
		target = new DropwizardContainer("richodemus/reader", Sets.newHashSet("YOUTUBE_URL=http://" + youtubeIp + ":" + port + "/"));
		baseUrl = "http://localhost:" + target.getHttpPort();
	}

	@After
	public void tearDown() throws Exception
	{
		target.close();
		youtubeMock.close();
	}

	@Test(timeout = 30_000L)
	public void test() throws Exception
	{
		final String username = "richodemus";
		final String feedName = "richodemus";

		RestAssured
				.given().body(username)
				.when().post(baseUrl + "/api/users")
				.then().assertThat().statusCode(200);

		final String token = RestAssured
				.given().body("123456789qwertyuio123qweasd")
				.when().post(baseUrl + "/api/users/" + username + "/sessions")
				.then().assertThat().statusCode(200).extract().body().jsonPath().get("token");

		RestAssured
				.given().header("x-token-jwt", token).body(feedName).contentType(ContentType.JSON)
				.when().post(baseUrl + "/api/users/" + username + "/feeds/")
				.then().assertThat().statusCode(204);

		RestAssured
				.given().header("x-token-jwt", token)
				.when().get(baseUrl + "/api/users/" + username + "/feeds/")
				.then().assertThat().statusCode(200);


		final int adminPort = target.getAdminPort();
		post("http://localhost:" + adminPort + "/tasks/download").then().statusCode(200);

		while(getItems(username, feedName, token).size() == 0)
		{
			Thread.sleep(100L);
		}
		System.out.println(target.getLogs());
	}

	private List<String> getItems(String username, String feedName, String token)
	{
		return RestAssured
				.given().header("x-token-jwt", token)
				.when().get(baseUrl + "/api/users/" + username + "/feeds/" + feedName)
				.then().assertThat().statusCode(200).extract().body().jsonPath().get("items");
	}
}
