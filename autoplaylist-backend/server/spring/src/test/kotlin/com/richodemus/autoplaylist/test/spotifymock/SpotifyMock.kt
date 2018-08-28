package com.richodemus.autoplaylist.test.spotifymock

import com.richodemus.autoplaylist.dto.Album
import com.richodemus.autoplaylist.dto.Artist
import com.richodemus.autoplaylist.dto.ArtistId
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.PlaylistName
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyPlaylistId
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.Track
import com.richodemus.autoplaylist.dto.TrackUri
import com.richodemus.autoplaylist.spotify.AccessToken
import com.richodemus.autoplaylist.spotify.Playlist
import com.richodemus.autoplaylist.spotify.SpotifyPort
import com.richodemus.autoplaylist.spotify.Tokens
import com.richodemus.autoplaylist.test.dto.PlaylistWithTracks
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@Primary // todo is this the right way?
class SpotifyMock : SpotifyPort {
    lateinit var oAuth2Code: String
    lateinit var spotifyUserId: SpotifyUserId
    private lateinit var accessToken: AccessToken
    private lateinit var refreshToken: RefreshToken
    private var playlists = emptyList<PlaylistWithTracks>()

    override suspend fun getToken(code: String): Tokens {
        if (code != oAuth2Code) {
            throw RuntimeException("Wrong code")
        }

        return Tokens(
                accessToken,
                "scope",
                "type",
                100000,
                refreshToken
        )
    }

    override suspend fun getUserId(accessToken: AccessToken): SpotifyUserId {
        if (accessToken != this.accessToken) {
            throw RuntimeException("Wrong accessToken")
        }

        return spotifyUserId
    }

    override suspend fun getPlaylists(accessToken: AccessToken): List<Playlist> {
        return playlists.map { it.toPlaylist() }
    }

    override suspend fun refreshToken(refreshToken: RefreshToken): Tokens {
        if (refreshToken != this.refreshToken) {
            throw RuntimeException("Wrong refreshToken")
        }

        return Tokens(
                accessToken,
                "scope",
                "type",
                100000,
                refreshToken
        )

    }

    override suspend fun findArtist(accessToken: AccessToken, name: ArtistName): List<Artist> {
        if (accessToken != this.accessToken) {
            throw RuntimeException("Wrong accessToken")
        }

        if (name == ARTIST_WITH_DUPLICATE_TRACKS.name) {
            return listOf(ARTIST_WITH_DUPLICATE_TRACKS.toArtist())
        }

        return listOf(ARTIST.toArtist())
    }

    override suspend fun getArtist(accessToken: AccessToken, artistId: ArtistId): Artist? {
        if (accessToken != this.accessToken) {
            throw RuntimeException("Wrong accessToken")
        }

        if (artistId == ARTIST_WITH_DUPLICATE_TRACKS.id) {
            return ARTIST_WITH_DUPLICATE_TRACKS.toArtist()
        }

        return ARTIST.toArtist()
    }

    override suspend fun getAlbums(accessToken: AccessToken, artistId: ArtistId): List<Album> {
        if (accessToken != this.accessToken) {
            throw RuntimeException("Wrong accessToken")
        }

        if (artistId == ARTIST_WITH_DUPLICATE_TRACKS.id) {
            return ARTIST_WITH_DUPLICATE_TRACKS.albums
        }

        return ARTIST.albums
    }

    override suspend fun getTracks(accessToken: AccessToken, playlistId: SpotifyPlaylistId): List<Track> {
        if (accessToken != this.accessToken) {
            throw RuntimeException("Wrong accessToken")
        }
        if (spotifyUserId != this.spotifyUserId) {
            throw RuntimeException("Wrong spotifyUserId")
        }
        if (playlists.none { it.id == playlistId }) {
            throw RuntimeException("No playlist with id $playlistId")
        }

        return playlists.first { it.id == playlistId }.tracks
    }

    override suspend fun createPlaylist(accessToken: AccessToken, name: PlaylistName): Playlist {
        if (accessToken != this.accessToken) {
            throw RuntimeException("Wrong accessToken")
        }

        val playlist = PlaylistWithTracks(SpotifyPlaylistId(UUID.randomUUID().toString()), name)
        playlists += playlist
        return playlist.toPlaylist()
    }

    override suspend fun addTracksToPlaylist(accessToken: AccessToken, playlistId: SpotifyPlaylistId, tracks: List<TrackUri>) {
        if (accessToken != this.accessToken) {
            throw RuntimeException("Wrong accessToken")
        }
        if (spotifyUserId != this.spotifyUserId) {
            throw RuntimeException("Wrong spotifyUserId")
        }
        if (playlists.none { it.id == playlistId }) {
            throw RuntimeException("No playlist with id $playlistId")
        }

        playlists = playlists.map { playlist ->
            if (playlist.id != playlistId) {
                return@map playlist
            }
            return@map playlist.copy(tracks = playlist.tracks + tracks.map { getTrack(it) })
        }
        return
    }

    fun reset() {
        oAuth2Code = UUID.randomUUID().toString()
        spotifyUserId = SpotifyUserId(UUID.randomUUID().toString())
        accessToken = AccessToken(UUID.randomUUID().toString())
        refreshToken = RefreshToken(UUID.randomUUID().toString())
        playlists = emptyList()
    }

    private fun PlaylistWithTracks.toPlaylist(): Playlist {
        return Playlist(this.id, this.name)
    }

    fun getPlaylist(spotifyPlaylistId: SpotifyPlaylistId): PlaylistWithTracks {
        return playlists.find { it.id == spotifyPlaylistId }
                ?: throw IllegalStateException("No playlist with id $spotifyPlaylistId")
    }
}
