package com.richodemus.autoplaylist.playlist

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.richodemus.autoplaylist.dto.Album
import com.richodemus.autoplaylist.dto.Artist
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.Track
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.spotify.AccessToken
import com.richodemus.autoplaylist.spotify.Playlist
import com.richodemus.autoplaylist.spotify.PlaylistId
import com.richodemus.autoplaylist.spotify.PlaylistName
import com.richodemus.autoplaylist.spotify.SpotifyPort
import com.richodemus.autoplaylist.spotify.Tokens
import com.richodemus.autoplaylist.user.User
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test

class UserTest {
    @Test
    fun `Should create playlist`() = runBlocking {
        val spotifyAdapter = mock<SpotifyPort> {
            onBlocking { refreshToken(any()) } doReturn Tokens(
                    AccessToken("a"),
                    "scope",
                    "type",
                    1000000,
                    RefreshToken("r")
            )

            onBlocking { findArtist(any(), any()) } doReturn emptyList<Artist>()
            onBlocking { getAlbums(any(), any()) } doReturn emptyList<Album>()
            onBlocking { getTracks(any(), any()) } doReturn emptyList<Track>()
            onBlocking { createPlaylist(any(), any()) } doReturn Playlist(PlaylistId("p"), PlaylistName("n"))

            onBlocking { addTracksToPlaylist(any(), any(), any()) } doReturn Unit
        }
        val user = User(
                mock {},
                spotifyAdapter,
                UserId(),
                SpotifyUserId("richodemus"),
                RefreshToken("r")
        )

        val playlistName = PlaylistName("Katten Skogmans Orkester (G)")
        val artist = ArtistName("Katten Skogmans Orkester")
        val playlist = user.createPlaylist(playlistName, artist, emptyList())
        playlist.sync()
        println("Playlist: ${playlist.id}, with tracks: ${playlist.albumsWithTracks()}")
    }
}
