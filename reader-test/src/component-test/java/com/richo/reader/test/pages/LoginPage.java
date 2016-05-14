package com.richo.reader.test.pages;

import com.jayway.restassured.RestAssured;

import java.util.Optional;

public class LoginPage
{
	private final String baseUrl;
	private Optional<String> token;
	private Optional<String> username;

	public LoginPage(final int port)
	{
		baseUrl = "http://localhost:" + port;
	}

	public void createUser(final String username)
	{
		RestAssured
				.given().body(username)
				.when().post(baseUrl + "/api/users")
				.then().assertThat().statusCode(200);
	}

	public void login(final String username)
	{
		login(username, "123456789qwertyuio123qweasd");
	}

	public void login(String username, String password)
	{
		this.username = Optional.of(username);
		this.token = Optional.ofNullable(RestAssured
				.given().body(password)
				.when().post(baseUrl + "/api/users/" + username + "/sessions")
				.then().extract().body().jsonPath().get("token"));
	}

	public boolean isLoggedIn()
	{
		return token.isPresent();
	}

	public FeedPage toFeedPage()
	{
		return new FeedPage(
				baseUrl,
				username.orElseThrow(() -> new RuntimeException("No username")),
				token.orElseThrow(() -> new RuntimeException("No token")));
	}
}
