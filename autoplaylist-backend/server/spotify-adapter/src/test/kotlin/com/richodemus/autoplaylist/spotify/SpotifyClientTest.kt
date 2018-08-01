package com.richodemus.autoplaylist.spotify

import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.RefreshToken
import io.github.vjames19.futures.jdk8.onFailure
import org.junit.Ignore
import org.junit.Test


class SpotifyClientTest {
    @Ignore
    @Test
    fun `Exceed rate limit`() {
        val client = SpotifyAdapter(SpotifyClient())

        val token = client.refreshToken(RefreshToken(System.getenv("REFRESH_TOKEN")))
        for (i in 0..1000) {
            Thread.sleep(10L)
            val result = client.findArtist(token.join().accessToken, ArtistName("Iced Earth"))
            result.onFailure {
                println(it)
            }
            println("$i: ${result.join()}")

        }
    }
}
