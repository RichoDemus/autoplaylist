package com.richodemus.autoplaylist.test

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.spotify.AccessToken
import com.richodemus.autoplaylist.spotify.PlayList
import com.richodemus.autoplaylist.spotify.PlayListId
import com.richodemus.autoplaylist.spotify.PlaylistName
import com.richodemus.autoplaylist.spotify.SpotifyPort
import com.richodemus.autoplaylist.spotify.Tokens
import com.richodemus.autoplaylist.test.pages.LoginPage
import io.github.vjames19.futures.jdk8.Future
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.junit4.SpringRunner
import java.util.UUID


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class ServiceTest {

    @LocalServerPort
    private var port: Int = -1

    @MockBean
    private lateinit var spotifyPort: SpotifyPort

    private lateinit var loginPage: LoginPage

    private lateinit var oAuth2Code: String

    private lateinit var spotifyUserId: String

    @Before
    fun setUp() {
        oAuth2Code = UUID.randomUUID().toString()
        spotifyUserId = UUID.randomUUID().toString()
        loginPage = LoginPage(port)
        whenever(spotifyPort.getToken(oAuth2Code)).doReturn(Future {
            Tokens(
                    AccessToken("access-token"),
                    "scope",
                    "type",
                    100000,
                    RefreshToken("refresh-token")
            )
        })
        whenever(spotifyPort.getUserId(AccessToken("access-token"))).doReturn(Future {
            SpotifyUserId(spotifyUserId)
        })
        whenever(spotifyPort.refreshToken(any())).doReturn(Future {
            Tokens(
                    AccessToken("access-token"),
                    "scope",
                    "type",
                    100000,
                    RefreshToken("refresh-token")
            )
        })
        whenever(spotifyPort.getPlaylists(any())).doReturn(Future {
            listOf(
                    PlayList(PlayListId("id1"), PlaylistName("name1")),
                    PlayList(PlayListId("id2"), PlaylistName("name2"))
            )
        })
    }

    @Test
    fun `Register new user`() {
        val mainPage = loginPage.login(oAuth2Code)
        val result = mainPage.getSpotifyUserId()

        assertThat(result).isEqualTo(SpotifyUserId(spotifyUserId))
    }

    @Test
    fun `Get playlists`() {
        val mainPage = loginPage.login(oAuth2Code)
        val result = mainPage.getPlaylists()

        val expected = listOf(
                PlayList(PlayListId("id1"), PlaylistName("name1")),
                PlayList(PlayListId("id2"), PlaylistName("name2"))
        )

        assertThat(result).isEqualTo(expected)
    }
}
