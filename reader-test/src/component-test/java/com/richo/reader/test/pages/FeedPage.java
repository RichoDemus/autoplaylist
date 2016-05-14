package com.richo.reader.test.pages;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import java.util.List;

public class FeedPage
{
	private final String baseUrl;
	private final String username;
	private final String token;

	FeedPage(String baseUrl, String username, String token)
	{
		this.baseUrl = baseUrl;
		this.username = username;
		this.token = token;
	}

	public List<String> getAllFeedNames()
	{
		return RestAssured
				.given().header("x-token-jwt", token)
				.when().get(baseUrl + "/api/users/" + username + "/feeds/")
				.then().assertThat().statusCode(200).extract().body().jsonPath().get("feeds.name");
	}

	public void addFeed(String feedName)
	{
		RestAssured
				.given().header("x-token-jwt", token).body(feedName).contentType(ContentType.JSON)
				.when().post(baseUrl + "/api/users/" + username + "/feeds/")
				.then().assertThat().statusCode(204);
	}

	public List<String> getItemNames(String feedName)
	{
		return RestAssured
				.given().header("x-token-jwt", token)
				.when().get(baseUrl + "/api/users/" + username + "/feeds/" + feedName)
				.then().extract().body().jsonPath().get("items.id");
	}
}
