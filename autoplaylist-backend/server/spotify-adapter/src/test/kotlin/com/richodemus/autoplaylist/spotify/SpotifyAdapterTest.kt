package com.richodemus.autoplaylist.spotify

import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.RefreshToken
import io.github.vjames19.futures.jdk8.map
import io.github.vjames19.futures.jdk8.onFailure
import org.junit.Ignore
import org.junit.Test


class SpotifyAdapterTest {
    @Ignore
    @Test
    fun `Exceed rate limit`() {
        val target = SpotifyAdapter(SpotifyClient("", "", "", "", ""))

        val token = target.refreshToken(RefreshToken(System.getenv("REFRESH_TOKEN")))
        for (i in 0..1000) {
            Thread.sleep(10L)
            val result = target.findArtist(token.join().accessToken, ArtistName("Iced Earth"))
            result.onFailure {
                println(it)
            }
            println("$i: ${result.join()}")

        }
    }

    @Ignore
    @Test
    fun `Find Artists`() {
        val target = SpotifyAdapter(SpotifyClient("", "", "", "", ""))

        val token = target.refreshToken(RefreshToken(System.getenv("REFRESH_TOKEN")))

        val artists = target.findArtist(token.join().accessToken, ArtistName("Queen")).join()

        println(artists)
    }

    @Ignore

    @Test
    fun `Investigate duplicates`() {
        val target = SpotifyAdapter(SpotifyClient("", "", "", "", ""))

        val token = target.refreshToken(RefreshToken(System.getenv("REFRESH_TOKEN")))

        val accessToken = token.join().accessToken
        val artists = target.findArtist(accessToken, ArtistName("Civil War"))
                .map { it.filter { artist -> artist.id.value == "6lGzC0JJCotCU9QZ2Lgi8T" } }
                .join()

        val artistsWithAlbums = artists
                .map { it to target.getAlbums(accessToken, it.id).join() }

        artistsWithAlbums.forEach {
            println(it.first)
            it.second.forEach { album ->
                println("\t${album.name}, ${album.tracks.size} tracks")
            }
            println()
        }
    }
}
