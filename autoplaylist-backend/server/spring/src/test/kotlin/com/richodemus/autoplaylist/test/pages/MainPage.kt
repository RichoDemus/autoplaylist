package com.richodemus.autoplaylist.test.pages

import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.spotify.PlayList
import io.restassured.RestAssured
import io.restassured.http.Cookie

class MainPage(private val port: Int, private val sessionId: Cookie) {
    fun getSpotifyUserId(): SpotifyUserId {
        val userId = RestAssured
                .given().cookie(sessionId)
                .`when`().get("http://localhost:$port/v1/users/me")
                .then().assertThat().statusCode(200).extract().jsonPath().getString("userId")

        return SpotifyUserId(userId)
    }

    fun getPlaylists(): List<PlayList> {
        return RestAssured
                .given().cookie(sessionId)
                .`when`().get("http://localhost:$port/v1/playlists")
                .then().assertThat().statusCode(200).extract().jsonPath().getList("", PlayList::class.java)
    }

}
