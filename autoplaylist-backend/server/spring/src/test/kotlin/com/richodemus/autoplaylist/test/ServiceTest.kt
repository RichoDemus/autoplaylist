package com.richodemus.autoplaylist.test

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.spotify.AccessToken
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


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class ServiceTest {

    @LocalServerPort
    private var port: Int = -1

    @MockBean
    private lateinit var spotifyPort: SpotifyPort

    private lateinit var loginPage: LoginPage

    @Before
    fun setUp() {
        loginPage = LoginPage(port)
        whenever(spotifyPort.getToken("my-fancy-code")).doReturn(Future {
            Tokens(
                    AccessToken("access-token"),
                    "scope",
                    "type",
                    100000,
                    RefreshToken("refresh-token")
            )
        })
        whenever(spotifyPort.getUserId(AccessToken("access-token"))).doReturn(Future {
            SpotifyUserId("spotify-user-id")
        })
    }

    @Test
    fun `Register new user`() {
        val mainPage = loginPage.login("my-fancy-code")
        val spotifyUserId = mainPage.getSpotifyUserId()

        assertThat(spotifyUserId).isEqualTo(SpotifyUserId("spotify-user-id"))
    }
}
