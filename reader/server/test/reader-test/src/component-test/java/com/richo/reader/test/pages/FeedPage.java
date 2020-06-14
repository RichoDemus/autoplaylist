package com.richo.reader.test.pages;

import com.richo.reader.test.pages.model.FeedId;
import com.richo.reader.test.pages.model.FeedUrl;
import com.richo.reader.test.pages.model.FeedWithoutItem;
import com.richo.reader.test.pages.model.Label;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

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

	public List<FeedWithoutItem> getAllFeeds()
	{
		return RestAssured
				.given().header("x-token-jwt", token)
				.when().get(baseUrl + "/api/users/" + username + "/feeds/")
				.then().assertThat().statusCode(200).extract().body().jsonPath().get("feeds");
	}

	public void addFeed(final FeedUrl feedUrl)
	{
		RestAssured
				.given().header("x-token-jwt", token).body(feedUrl.toJson()).contentType(ContentType.JSON)
				.when().post(baseUrl + "/api/users/" + username + "/feeds/")
				.then().assertThat().statusCode(204);
	}

	public List<String> getItemNames(final FeedId feedName)
	{
		return RestAssured
				.given().header("x-token-jwt", token)
				.when().get(baseUrl + "/api/users/" + username + "/feeds/" + feedName)
				.then().extract().body().jsonPath().get("items.id");
	}

	public void markAsRead(FeedId feedName, String item)
	{
		RestAssured
				.given().header("x-token-jwt", token).body(new MarkReadAction()).contentType(ContentType.JSON)
				.when().post(baseUrl + "/api/users/" + username + "/feeds/" + feedName + "/items/" + item)
				.then().assertThat().statusCode(204);
	}

	public String createLabel(String labelName)
	{
		return RestAssured
				.given().header("x-token-jwt", token).body(labelName).contentType(ContentType.JSON)
				.when().post(baseUrl + "/api/users/" + username + "/labels/")
				.then().extract().body().jsonPath().get("id");
	}

	public void addFeedToLabel(final FeedId feedName, String labelId)
	{
		RestAssured
				.given().header("x-token-jwt", token).body(feedName.toJson()).contentType(ContentType.JSON)
				.when().put(baseUrl + "/api/users/" + username + "/labels/" + labelId)
				.then().assertThat().statusCode(204);
	}

	public List<Label> getLabels()
	{
		return RestAssured
				.given().header("x-token-jwt", token)
				.when().get(baseUrl + "/api/users/" + username + "/feeds/")
				.then().assertThat().statusCode(200).extract().body().jsonPath().get("labels");
	}

	private static class MarkReadAction
	{
		public final String action = "MARK_READ";
	}
}
