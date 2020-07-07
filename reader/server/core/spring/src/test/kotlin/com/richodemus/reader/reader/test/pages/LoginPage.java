package com.richodemus.reader.reader.test.pages;

import com.google.common.collect.ImmutableMap;
import io.restassured.RestAssured;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginPage {
    private static final String DEFAULT_PASSWORD = "123456789qwertyuio123qweasd";
    private static final String INVITE_CODE = "testcode";
    private final String baseUrl;
    private Optional<String> token;
    public Optional<String> username;
    private Map<String, String> session = Map.of();

    public LoginPage(final int port) {
        baseUrl = "http://localhost:" + port;
    }

    public void createUser() {
        var username = UUID.randomUUID().toString();
        final int status = createUser(username, DEFAULT_PASSWORD, INVITE_CODE);
        assertThat(status).isEqualTo(200);
    }

    public int createUser(final String username) {
        final int status = createUser(username, DEFAULT_PASSWORD, INVITE_CODE);
        assertThat(status).isEqualTo(200);
        return status;
    }

    public int createUser(final String username, final String inviteCode) {
        return createUser(username, DEFAULT_PASSWORD, inviteCode);
    }

    private int createUser(final String username, final String password, final String inviteCode) {
        this.username = Optional.of(username);
        return RestAssured
                .given().contentType("application/json")
                .body(ImmutableMap.builder()
                        .put("username", username).put("password", password)
                        .put("inviteCode", inviteCode)
                        .build())
                .when().post(baseUrl + "/v1/users")
                .then().extract().statusCode();
    }

    public boolean loginWithPassword(String password) {
        return login(username.get(), password);
    }

    public boolean login() {
        return login(username.get());
    }

    public boolean login(final String username) {
        return login(username, DEFAULT_PASSWORD);
    }

    public boolean login(String username, String password) {
        this.username = Optional.of(username);
        var then = RestAssured
                .given().contentType("application/json").body("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")
                .when().post(baseUrl + "/v1/sessions")
                .then();
        this.session = then.extract().cookies();
        return then.extract().statusCode() == 200;
    }

    public void downloadFeeds() {
        RestAssured
                .given().cookies(session)
                .post(baseUrl + "/v1/admin/download")
                .then().statusCode(200);
    }

    public void refreshToken() {
        final String token = this.token.orElseThrow(RuntimeException::new);
        final String user = username.orElseThrow(RuntimeException::new);
        this.token = Optional.ofNullable(RestAssured
                .given().header("x-token-jwt", token).body("")
                .when().post(baseUrl + "/v1/users/" + user + "/sessions/refresh")
                .then().extract().body().jsonPath().get("token"));
    }

    public boolean isLoggedIn() {
        var then = RestAssured
                .given().cookies(session)
                .when().get(baseUrl + "/v1/sessions")
                .then();
        return then.extract().statusCode() == 200;
    }

    public FeedPage toFeedPage() {
        return new FeedPage(
                baseUrl,
                username.orElseThrow(() -> new RuntimeException("No username")),
                session);
    }

    public void setToken(final String token) {
        this.token = Optional.of(token);
    }

    public void setUsername(final String username) {
        this.username = Optional.of(username);
    }

    public String getToken() {
        return token.get();
    }
}
