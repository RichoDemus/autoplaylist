package com.richodemus.autoplaylist.test

import com.richodemus.autoplaylist.spotify.PlaylistName
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
internal class PlaylistTest {

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
    fun `Get playlists when there is no playlists`() {
        val mainPage = loginPage.login(spotifyPort.oAuth2Code)

        val result = mainPage.getPlaylists()

        assertThat(result).isEmpty()
    }

    @Test
    fun `Create playlist`() {
        val playlistName = PlaylistName("new-playlist")

        val mainPage = loginPage.login(spotifyPort.oAuth2Code)
        val result = mainPage.createPlaylist(playlistName, ARTIST.name)

        assertThat(result.albums).isEqualTo(ARTIST.albums)

        val playlist = mainPage.getPlaylists()
                .find { it.name == playlistName }
                ?: throw AssertionError("Couldn't find playlist")

        val tracks = mainPage.getTracks(playlist.id)

        val expectedTracks = ARTIST.albums.flatMap { it.tracks }
        assertThat(tracks).containsOnlyElementsOf(expectedTracks)
    }
}
