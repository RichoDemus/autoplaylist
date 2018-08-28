package com.richodemus.autoplaylist.test.pages

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.richodemus.autoplaylist.dto.Artist
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.Playlist
import com.richodemus.autoplaylist.dto.PlaylistId
import com.richodemus.autoplaylist.dto.PlaylistName
import com.richodemus.autoplaylist.dto.Rules
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.Track
import io.restassured.RestAssured
import io.restassured.http.ContentType.JSON
import io.restassured.http.Cookie

class MainPage(private val port: Int, private val sessionId: Cookie) {
    fun getSpotifyUserId(): SpotifyUserId {
        val userId = RestAssured
                .given().cookie(sessionId)
                .`when`().get("http://localhost:$port/v1/users/me")
                .then().assertThat().statusCode(200).extract().jsonPath().getString("userId")

        return SpotifyUserId(userId)
    }

    fun getPlaylists(): List<Playlist> {
        return RestAssured
                .given().cookie(sessionId)
                .`when`().get("http://localhost:$port/v1/playlists")
                .then().assertThat().statusCode(200).extract().jsonPath().getList("", Playlist::class.java)
    }

    fun createPlaylist(
            playlistName: PlaylistName
    ): Playlist {
        data class CreatePlaylistRequest(val name: PlaylistName)

        val json = jacksonObjectMapper().writeValueAsString(CreatePlaylistRequest(playlistName))
        return RestAssured
                .given().cookie(sessionId).body(json).contentType(JSON)
                .`when`().post("http://localhost:$port/v1/playlists")
                .then().assertThat().statusCode(200).extract()
                .jsonPath().getObject("", CreatePlaylistResponse::class.java)
                .playlist ?: throw IllegalStateException("Failed to create playlist")
    }

    fun getPlaylist(id: PlaylistId): Playlist {
        return RestAssured
                .given().cookie(sessionId)
                .`when`().get("http://localhost:$port/v1/playlists/$id")
                .then().assertThat().statusCode(200).extract().jsonPath().getObject("", Playlist::class.java)
    }

    fun findArtists(artistName: ArtistName): List<Artist> {
        return RestAssured
                .given().cookie(sessionId).queryParam("name", artistName.value)
                .`when`().get("http://localhost:$port/v1/artists")
                .then().assertThat().statusCode(200).extract().jsonPath().getList("", Artist::class.java)
    }

    fun getTracks(id: PlaylistId): List<Track> {
        return RestAssured
                .given().cookie(sessionId)
                .`when`().get("http://localhost:$port/v1/playlists/$id/tracks")
                .then().assertThat().statusCode(200).extract().jsonPath().getList("", Track::class.java)
    }

    fun setPlaylistRules(
            id: PlaylistId,
            rules: Rules
    ): Playlist {
        val json = jacksonObjectMapper().writeValueAsString(rules)
        println(json)
        return RestAssured
                .given().cookie(sessionId).body(json).contentType(JSON)
                .`when`().post("http://localhost:$port/v1/playlists/$id/rules")
                .then().assertThat().statusCode(200).extract()
                .jsonPath().getObject("", Playlist::class.java)
    }

    fun setSync(id: PlaylistId, enabled: Boolean): Playlist {
        return RestAssured
                .given().cookie(sessionId).body("\"$enabled\"").contentType(JSON)
                .`when`().post("http://localhost:$port/v1/playlists/$id/sync")
                .then().assertThat().statusCode(200).extract()
                .jsonPath().getObject("", Playlist::class.java)
    }
}
