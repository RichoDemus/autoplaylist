package com.richodemus.autoplaylist.test

import com.richodemus.autoplaylist.dto.Artist
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.test.pages.LoginPage
import com.richodemus.autoplaylist.test.spotifymock.ARTIST
import com.richodemus.autoplaylist.test.spotifymock.SpotifyMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.junit4.SpringRunner
import javax.inject.Inject


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class ServiceTest {

    @LocalServerPort
    private var port: Int = -1

    @Inject
    private lateinit var spotifyPort: SpotifyMock

    private lateinit var loginPage: LoginPage

    @Before
    fun setUp() {
        loginPage = LoginPage(port)
        spotifyPort.reset()
    }

    @Test
    fun `Register new user`() {
        val mainPage = loginPage.login(spotifyPort.oAuth2Code)

        val result = mainPage.getSpotifyUserId()

        assertThat(result).isEqualTo(SpotifyUserId(spotifyPort.spotifyUserId.value))
    }

    // todo write logout test

    @Test
    fun `Find Artist`() {
        val mainPage = loginPage.login(spotifyPort.oAuth2Code)

        val result = mainPage.findArtists(ARTIST.name)

        assertThat(result).containsOnly(Artist(ARTIST.id, ARTIST.name))
    }
}
