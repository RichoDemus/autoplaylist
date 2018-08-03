package com.richodemus.autoplaylist.spotify

import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.RefreshToken
import io.github.vjames19.futures.jdk8.onFailure
import org.junit.Ignore
import org.junit.Test


class SpotifyAdapterTest {
    @Ignore
    @Test
    fun `Exceed rate limit`() {
        val target = SpotifyAdapter(SpotifyClient())

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
        val target = SpotifyAdapter(SpotifyClient())

        val token = target.refreshToken(RefreshToken(System.getenv("REFRESH_TOKEN")))

        val artists = target.findArtist(token.join().accessToken, ArtistName("Queen")).join()

        println(artists)
    }
}
