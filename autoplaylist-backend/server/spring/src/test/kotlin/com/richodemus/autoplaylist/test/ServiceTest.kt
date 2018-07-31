package com.richodemus.autoplaylist.test

import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.playlist.PlaylistWithAlbums
import com.richodemus.autoplaylist.spotify.PlayList
import com.richodemus.autoplaylist.spotify.PlayListId
import com.richodemus.autoplaylist.spotify.PlaylistName
import com.richodemus.autoplaylist.spotify.SpotifyPort
import com.richodemus.autoplaylist.test.pages.LoginPage
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
        spotifyPort.mockDefaultBehavior()
    }

    @Test
    fun `Register new user`() {
        val mainPage = loginPage.login(spotifyPort.oAuth2Code)
        val result = mainPage.getSpotifyUserId()

        assertThat(result).isEqualTo(SpotifyUserId(spotifyPort.spotifyUserId))
    }

    @Test
    fun `Get playlists`() {
        val mainPage = loginPage.login(spotifyPort.oAuth2Code)
        val result = mainPage.getPlaylists()

        val expected = listOf(
                PlayList(PlayListId("id1"), PlaylistName("name1")),
                PlayList(PlayListId("id2"), PlaylistName("name2"))
        )

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `Create playlist`() {
        val mainPage = loginPage.login(spotifyPort.oAuth2Code)
        val result = mainPage.createPlaylist(PlaylistName("new-playlist"), ARTIST.name)

        assertThat(result).isEqualTo(PlaylistWithAlbums(ARTIST.albums))
        // assertThat(mainPage.getPlaylists()).extracting("name").contains("new-playlist")
    }
}
