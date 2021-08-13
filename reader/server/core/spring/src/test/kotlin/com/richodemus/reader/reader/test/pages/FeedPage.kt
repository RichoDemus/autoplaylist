package com.richodemus.reader.reader.test.pages

import com.richodemus.reader.reader.test.pages.model.FeedId
import com.richodemus.reader.reader.test.pages.model.FeedUrl
import com.richodemus.reader.reader.test.pages.model.FeedWithoutItem
import com.richodemus.reader.reader.test.pages.model.Label
import io.restassured.RestAssured
import io.restassured.http.ContentType

class FeedPage internal constructor(private val baseUrl: String, private val username: String, private val session: Map<String, String?>) {
    val allFeeds: List<FeedWithoutItem>
        get() = RestAssured
                .given().cookies(session)
                .`when`()["$baseUrl/v1/feeds/"]
                .then().assertThat().statusCode(200).extract().body().jsonPath().get("feeds")

    fun addFeed(feedUrl: FeedUrl) {
        RestAssured
                .given().cookies(session).body(feedUrl.toJson()).contentType(ContentType.JSON)
                .`when`().post("$baseUrl/v1/feeds/")
                .then().assertThat().statusCode(200)
    }

    fun getItemNames(feedName: FeedId): List<String> {
        return RestAssured
                .given().cookies(session)
                .`when`()["$baseUrl/v1/feeds/$feedName"]
                .then().extract().body().jsonPath().get("items.id")
    }

    fun markAsRead(feedName: FeedId, item: String) {
        RestAssured
                .given().cookies(session).body(MarkReadAction()).contentType(ContentType.JSON)
                .`when`().post("$baseUrl/v1/feeds/$feedName/items/$item")
                .then().assertThat().statusCode(200)
    }

    fun createLabel(labelName: String?): String {
        return RestAssured
                .given().cookies(session).body(labelName).contentType(ContentType.JSON)
                .`when`().post("$baseUrl/v1/labels/")
                .then()
                .assertThat().statusCode(200)
                .extract().body().jsonPath().get("id")
    }

    fun addFeedToLabel(feedName: FeedId, labelId: String) {
        RestAssured
                .given().cookies(session).body(feedName.toJson()).contentType(ContentType.JSON)
                .`when`().post("$baseUrl/v1/labels/$labelId")
                .then().assertThat().statusCode(200)
    }

    val labels: List<Label>
        get() = RestAssured
                .given().cookies(session)
                .`when`()["$baseUrl/v1/feeds/"]
                .then().assertThat().statusCode(200).extract().body().jsonPath().get("labels")

    private class MarkReadAction {
        val action = "MARK_READ"
    }
}
