package com.richodemus.autoplaylist.spotify

import com.richodemus.autoplaylist.dto.ArtistId
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.RefreshToken
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test


class SpotifyAdapterTest {
    @Ignore
    @Test
    fun `Exceed rate limit`() = runBlocking {
        val target = SpotifyAdapter(SpotifyClient("", "", "", "", ""))

        val token = target.refreshToken(RefreshToken(System.getenv("REFRESH_TOKEN")))
        for (i in 0..1000) {
            Thread.sleep(10L)
            val result = target.findArtist(token.accessToken, ArtistName("Iced Earth"))
            println("$i: $result")
        }
    }

    @Ignore
    @Test
    fun `Find Artists`() = runBlocking {
        val target = SpotifyAdapter(SpotifyClient("", "", "", "", ""))

        val token = target.refreshToken(RefreshToken(System.getenv("REFRESH_TOKEN")))

        val artists = target.findArtist(token.accessToken, ArtistName("Queen"))

        println(artists)
    }

    @Ignore
    @Test
    fun `Get Tracks`() = runBlocking {
        val target = SpotifyAdapter(SpotifyClient(
                "https://api.spotify.com",
                "https://accounts.spotify.com",
                "df0732a2defe44ecabd30868fa57a2d5",
                "",
                "http://localhost:3000/callback"
        ))

        val token = target.refreshToken(RefreshToken(System.getenv("REFRESH_TOKEN")))

        val results = target.getAlbums(token.accessToken, ArtistId("0LcJLqbBmaGUft1e9Mm8HV"))

        println(results)
        results.forEach { println(it) }

    }

    @Ignore
    @Test
    fun `Investigate duplicates`() = runBlocking {
        val target = SpotifyAdapter(SpotifyClient("", "", "", "", ""))

        val token = target.refreshToken(RefreshToken(System.getenv("REFRESH_TOKEN")))

        val accessToken = token.accessToken
        val artists = target.findArtist(accessToken, ArtistName("Civil War"))
                .filter { artist -> artist.id.value == "6lGzC0JJCotCU9QZ2Lgi8T" }

        val artistsWithAlbums = artists
                .map { it to target.getAlbums(accessToken, it.id) }

        artistsWithAlbums.forEach {
            println(it.first)
            it.second.forEach { album ->
                println("\t${album.name}, ${album.tracks.size} tracks")
            }
            println()
        }
    }
}
