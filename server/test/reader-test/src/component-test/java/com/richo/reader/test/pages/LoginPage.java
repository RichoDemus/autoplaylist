package com.richo.reader.test.pages;

import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.RestAssured;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginPage
{
	private static final String DEFAULT_PASSWORD = "123456789qwertyuio123qweasd";
	private static final String INVITE_CODE = "iwouldlikeaninvitepleaseletmesignuptotestthis";
	private final String baseUrl;
	private Optional<String> token;
	private Optional<String> username;

	public LoginPage(final int port)
	{
		baseUrl = "http://localhost:" + port;
	}

	public int createUser(final String username)
	{
		final int status = createUser(username, DEFAULT_PASSWORD, INVITE_CODE);
		assertThat(status).isEqualTo(200);
		return status;
	}

	public int createUser(final String username, final String inviteCode)
	{
		return createUser(username, DEFAULT_PASSWORD, inviteCode);
	}

	private int createUser(final String username, final String password, final String inviteCode)
	{
		return RestAssured
				.given().contentType("application/json")
				.body(ImmutableMap.builder()
						.put("username", username).put("password", password)
						.put("inviteCode", inviteCode)
						.build())
				.when().post(baseUrl + "/api/users")
				.then().extract().statusCode();
	}

	public void login(final String username)
	{
		login(username, DEFAULT_PASSWORD);
	}

	public void login(String username, String password)
	{
		this.username = Optional.of(username);
		this.token = Optional.ofNullable(RestAssured
				.given().body(password)
				.when().post(baseUrl + "/api/users/" + username + "/sessions")
				.then().extract().body().jsonPath().get("token"));
	}

	public void refreshToken()
	{
		final String token = this.token.orElseThrow(RuntimeException::new);
		final String user = username.orElseThrow(RuntimeException::new);
		this.token = Optional.ofNullable(RestAssured
				.given().header("x-token-jwt", token).body("")
				.when().post(baseUrl + "/api/users/" + user + "/sessions/refresh")
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

	public void setToken(final String token)
	{
		this.token = Optional.of(token);
	}

	public void setUsername(final String username)
	{
		this.username = Optional.of(username);
	}

	public String getToken()
	{
		return token.get();
	}
}
