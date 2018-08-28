package com.richodemus.autoplaylist.test

import com.richodemus.autoplaylist.dto.PlaylistName
import com.richodemus.autoplaylist.dto.Rules
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
import java.time.ZonedDateTime
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
    fun `Newly created playlist should be empty and not sync`() {
        val playlistName = PlaylistName("new-playlist")

        val mainPage = loginPage.login(spotifyPort.oAuth2Code)
        val result = mainPage.createPlaylist(playlistName)

        assertThat(result.albums).isEmpty()
        assertThat(result.rules.artists).isEmpty()
        assertThat(result.rules.exclusions).isEmpty()
        assertThat(result.sync).isFalse()
        assertThat(result.lastSynced).isEqualTo("1970-01-01T00:00:00Z")
        assertThat(result.spotifyPlaylistId).isNull()

        val alsoResult = mainPage.getPlaylist(result.id)

        assertThat(alsoResult).isEqualTo(result)
    }

    @Test
    fun `Set rules on a playlist and see tracks`() {
        val playlistName = PlaylistName("new-playlist")

        val mainPage = loginPage.login(spotifyPort.oAuth2Code)
        val id = mainPage.createPlaylist(playlistName).id

        val rules = Rules(listOf(ARTIST.id), emptyList())
        val result = mainPage.setPlaylistRules(id, rules)

        assertThat(result.albums.flatMap { it.tracks }).containsOnly(*ARTIST.albums.flatMap { it.tracks }.toTypedArray())
        assertThat(result.rules.artists).containsOnly(ARTIST.id)
        assertThat(result.rules.exclusions).isEmpty()
        assertThat(result.sync).`as`("is sync enabled").isFalse()
        assertThat(result.lastSynced).isEqualTo("1970-01-01T00:00:00Z")

        val alsoResult = mainPage.getPlaylist(result.id)

        assertThat(alsoResult).isEqualTo(result)
    }

    @Test
    fun `Enable syncing and see results`() {
        val timeWhenTestStarted = ZonedDateTime.now()
        val playlistName = PlaylistName("new-playlist")
        val mainPage = loginPage.login(spotifyPort.oAuth2Code)
        val id = mainPage.createPlaylist(playlistName).id


        mainPage.setPlaylistRules(id, Rules(listOf(ARTIST.id), emptyList()))
        mainPage.setSync(id, true)

        // Check playlist
        val result = mainPage.getPlaylist(id)
        assertThat(result.sync).isTrue()
        assertThat(ZonedDateTime.parse(result.lastSynced)).isAfter(timeWhenTestStarted)
        assertThat(result.albums.flatMap { it.tracks }).containsOnly(*ARTIST.albums.flatMap { it.tracks }.toTypedArray())

        // Check playlist at Spotify
        val playlistAtSpotify = spotifyPort.getPlaylist(result.spotifyPlaylistId!!)
        assertThat(playlistAtSpotify.tracks).containsOnly(*ARTIST.albums.flatMap { it.tracks }.toTypedArray())
    }
}
