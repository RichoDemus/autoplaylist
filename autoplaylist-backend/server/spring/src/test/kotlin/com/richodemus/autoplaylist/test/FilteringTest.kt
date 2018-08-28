package com.richodemus.autoplaylist.test

import com.richodemus.autoplaylist.dto.Album
import com.richodemus.autoplaylist.dto.Exclusion
import com.richodemus.autoplaylist.dto.ExclusionId
import com.richodemus.autoplaylist.dto.Keyword
import com.richodemus.autoplaylist.dto.PlaylistName
import com.richodemus.autoplaylist.dto.Rules
import com.richodemus.autoplaylist.dto.Track
import com.richodemus.autoplaylist.dto.TrackName
import com.richodemus.autoplaylist.test.pages.LoginPage
import com.richodemus.autoplaylist.test.spotifymock.ARTIST
import com.richodemus.autoplaylist.test.spotifymock.ARTIST_WITH_DUPLICATE_TRACKS
import com.richodemus.autoplaylist.test.spotifymock.SpotifyMock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.iterable.Extractor
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.junit4.SpringRunner
import java.util.UUID
import javax.inject.Inject

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class FilteringTest {

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
    fun `Deduplicate tracks with the same name`() {
        val playlistName = PlaylistName("new-playlist")
        val mainPage = loginPage.login(spotifyPort.oAuth2Code)
        val playlist = mainPage.createPlaylist(playlistName)
        mainPage.setPlaylistRules(playlist.id, Rules(listOf(ARTIST_WITH_DUPLICATE_TRACKS.id)))
        val result = mainPage.getPlaylist(playlist.id)

        assertThat(result.albums).flatExtracting(tracks).extracting(name).doesNotHaveDuplicates()
    }

    // todo move to some kind of rule-test
    @Test
    fun `Exclude tracks by pattern`() {
        val playlistName = PlaylistName("new-playlist")

        val mainPage = loginPage.login(spotifyPort.oAuth2Code)
        // The songs are [Armata Strigoi, Army of the Night, Amen and Attack, Kreuzfeuer]
        val playlist = mainPage.createPlaylist(playlistName
        )

        mainPage.setPlaylistRules(playlist.id, Rules(listOf(ARTIST.id), listOf(
                Exclusion(ExclusionId(UUID.randomUUID()), Keyword("army of")),
                Exclusion(ExclusionId(UUID.randomUUID()), Keyword("EuZfEu"))
        )))

        val result = mainPage.getPlaylist(playlist.id)

        assertThat(result.albums).flatExtracting<Track> { it.tracks }.extracting(name).containsOnly(
                TrackName("Armata Strigoi"),
                TrackName("Amen and Attack")
        )
    }

    private val name = Extractor<Track, TrackName> { it.name }
    private val tracks = Extractor<Album, List<Track>> { it.tracks }
}
