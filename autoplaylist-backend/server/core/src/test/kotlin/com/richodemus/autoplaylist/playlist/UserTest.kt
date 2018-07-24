package com.richodemus.autoplaylist.playlist

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.richodemus.autoplaylist.dto.Album
import com.richodemus.autoplaylist.dto.ArtistId
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.TrackId
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.spotify.AccessToken
import com.richodemus.autoplaylist.spotify.PlayList
import com.richodemus.autoplaylist.spotify.PlayListId
import com.richodemus.autoplaylist.spotify.PlaylistName
import com.richodemus.autoplaylist.spotify.SnapshotId
import com.richodemus.autoplaylist.spotify.SpotifyPort
import com.richodemus.autoplaylist.spotify.Tokens
import com.richodemus.autoplaylist.user.User
import io.github.vjames19.futures.jdk8.Future
import org.junit.Test

class UserTest {
    @Test
    fun `Should create playlist`() {
        val spotifyAdapter = mock<SpotifyPort> {
            on { refreshToken(any()) } doReturn Future {
                Tokens(
                        AccessToken("a"),
                        "scope",
                        "type",
                        1000000,
                        RefreshToken("r")
                )
            }
            on { findArtist(any(), any()) } doReturn Future { emptyList<ArtistId>() }
            on { getAlbums(any(), any()) } doReturn Future { emptyList<Album>() }
            on { getTracks(any(), any(), any()) } doReturn Future { emptyList<TrackId>() }
            on { createPlaylist(any(), any(), any()) } doReturn Future {
                PlayList(PlayListId("p"), PlaylistName("n"))
            }
            on { addTracksToPlaylist(any(), any(), any(), any()) } doReturn Future { Unit }
        }
        val user = User(
                mock {},
                spotifyAdapter,
                UserId(),
                SpotifyUserId("richodemus"),
                RefreshToken("AQCdDtlGLaop3IPKc0KkyuVDtwXB5K0Qofy4cocXy8cHg1lmW5k2rjYaqJ3KXT0d118kFTw88yUsV-kX_IXSDOD0d_VGnNcMJ07aWZ5XApFKoyyO62VDcXDENNJOyWp6uAI")
        )

        val playlistName = PlaylistName("Katten Skogmans Orkester (G)")
        val artist = ArtistName("Katten Skogmans Orkester")
        val result = user.createPlaylist(playlistName, artist)
        val playlist = result.join()
        playlist.sync()
        println("Playlist: ${playlist.id}, with tracks: ${playlist.albumsWithTracks().join()}")
    }
}
