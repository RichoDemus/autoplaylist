package com.richodemus.autoplaylist.test.pages

import com.richodemus.autoplaylist.dto.Artist
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.Track
import com.richodemus.autoplaylist.playlist.PlaylistWithAlbums
import com.richodemus.autoplaylist.spotify.Playlist
import com.richodemus.autoplaylist.spotify.PlaylistId
import com.richodemus.autoplaylist.spotify.PlaylistName
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

    fun createPlaylist(playlistName: PlaylistName, artistName: ArtistName): PlaylistWithAlbums {
        return RestAssured
                .given().cookie(sessionId).body("""{"name":"$playlistName","artist":"$artistName"}""").contentType(JSON)
                .`when`().post("http://localhost:$port/v1/playlists")
                .then().assertThat().statusCode(200).extract().jsonPath().getObject("playList", PlaylistWithAlbums::class.java)
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
}
