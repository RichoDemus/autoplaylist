package com.richodemus.reader.reader.test.pages;

import com.richodemus.reader.reader.test.pages.model.FeedId;
import com.richodemus.reader.reader.test.pages.model.FeedUrl;
import com.richodemus.reader.reader.test.pages.model.FeedWithoutItem;
import com.richodemus.reader.reader.test.pages.model.Label;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import java.util.List;
import java.util.Map;

public class FeedPage {
    private final String baseUrl;
    private final String username;
    private final Map<String, String> session;

    FeedPage(String baseUrl, String username, Map<String, String> session) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.session = session;
    }

    public List<FeedWithoutItem> getAllFeeds() {
        return RestAssured
                .given().cookies(session)
                .when().get(baseUrl + "/v1/feeds/")
                .then().assertThat().statusCode(200).extract().body().jsonPath().get("feeds");
    }

    public void addFeed(final FeedUrl feedUrl) {
        RestAssured
                .given().cookies(session).body(feedUrl.toJson()).contentType(ContentType.JSON)
                .when().post(baseUrl + "/v1/feeds/")
                .then().assertThat().statusCode(200);
    }

    public List<String> getItemNames(final FeedId feedName) {
        return RestAssured
                .given().cookies(session)
                .when().get(baseUrl + "/v1/feeds/" + feedName)
                .then().extract().body().jsonPath().get("items.id");
    }

    public void markAsRead(FeedId feedName, String item) {
        RestAssured
                .given().cookies(session).body(new MarkReadAction()).contentType(ContentType.JSON)
                .when().post(baseUrl + "/v1/feeds/" + feedName + "/items/" + item)
                .then().assertThat().statusCode(200);
    }

    public String createLabel(String labelName) {
        return RestAssured
                .given().cookies(session).body(labelName).contentType(ContentType.JSON)
                .when().post(baseUrl + "/v1/labels/")
                .then()
                .assertThat().statusCode(200)
                .extract().body().jsonPath().get("id");
    }

    public void addFeedToLabel(final FeedId feedName, String labelId) {
        RestAssured
                .given().cookies(session).body(feedName.toJson()).contentType(ContentType.JSON)
                .when().post(baseUrl + "/v1/labels/" + labelId)
                .then().assertThat().statusCode(200);
    }

    public List<Label> getLabels() {
        return RestAssured
                .given().cookies(session)
                .when().get(baseUrl + "/v1/feeds/")
                .then().assertThat().statusCode(200).extract().body().jsonPath().get("labels");
    }

    private static class MarkReadAction {
        public final String action = "MARK_READ";
    }
}
