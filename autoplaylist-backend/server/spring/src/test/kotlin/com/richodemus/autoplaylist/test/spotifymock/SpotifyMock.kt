package com.richodemus.autoplaylist.test.spotifymock

import com.richodemus.autoplaylist.dto.Album
import com.richodemus.autoplaylist.dto.Artist
import com.richodemus.autoplaylist.dto.ArtistId
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.Track
import com.richodemus.autoplaylist.dto.TrackUri
import com.richodemus.autoplaylist.spotify.AccessToken
import com.richodemus.autoplaylist.spotify.Playlist
import com.richodemus.autoplaylist.spotify.PlaylistId
import com.richodemus.autoplaylist.spotify.PlaylistName
import com.richodemus.autoplaylist.spotify.SpotifyPort
import com.richodemus.autoplaylist.spotify.Tokens
import com.richodemus.autoplaylist.test.dto.PlaylistWithTracks
import io.github.vjames19.futures.jdk8.Future
import io.github.vjames19.futures.jdk8.toCompletableFuture
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.CompletableFuture

@Service
@Primary // todo is this the right way?
class SpotifyMock : SpotifyPort {
    lateinit var oAuth2Code: String
    lateinit var spotifyUserId: SpotifyUserId
    private lateinit var accessToken: AccessToken
    private lateinit var refreshToken: RefreshToken
    private var playlists = emptyList<PlaylistWithTracks>()

    override fun getToken(code: String): CompletableFuture<Tokens> {
        if (code != oAuth2Code) {
            return RuntimeException("Wrong code").toCompletableFuture()
        }

        return Future {
            Tokens(
                    accessToken,
                    "scope",
                    "type",
                    100000,
                    refreshToken
            )
        }
    }

    override fun getUserId(accessToken: AccessToken): CompletableFuture<SpotifyUserId> {
        if (accessToken != this.accessToken) {
            return RuntimeException("Wrong accessToken").toCompletableFuture()
        }

        return Future { spotifyUserId }
    }

    override fun getPlaylists(accessToken: AccessToken): CompletableFuture<List<Playlist>> {
        return Future { playlists.map { it.toPlaylist() } }
    }

    override fun refreshToken(refreshToken: RefreshToken): CompletableFuture<Tokens> {
        if (refreshToken != this.refreshToken) {
            return RuntimeException("Wrong refreshToken").toCompletableFuture()
        }

        return Future {
            Tokens(
                    accessToken,
                    "scope",
                    "type",
                    100000,
                    refreshToken
            )
        }
    }

    override fun findArtist(accessToken: AccessToken, name: ArtistName): CompletableFuture<List<Artist>> {
        if (accessToken != this.accessToken) {
            return RuntimeException("Wrong accessToken").toCompletableFuture()
        }

        if (name == ARTIST_WITH_DUPLICATE_ALBUMS.name) {
            return Future { listOf(ARTIST_WITH_DUPLICATE_ALBUMS.toArtist()) }
        }

        return Future { listOf(ARTIST.toArtist()) }
    }

    override fun getAlbums(accessToken: AccessToken, artistId: ArtistId): CompletableFuture<List<Album>> {
        if (accessToken != this.accessToken) {
            return RuntimeException("Wrong accessToken").toCompletableFuture()
        }

        if (artistId == ARTIST_WITH_DUPLICATE_ALBUMS.id) {
            return Future { ARTIST_WITH_DUPLICATE_ALBUMS.albums }
        }

        return Future { ARTIST.albums }
    }

    override fun getTracks(accessToken: AccessToken, spotifyUserId: SpotifyUserId, playlistId: PlaylistId): CompletableFuture<List<Track>> {
        if (accessToken != this.accessToken) {
            return RuntimeException("Wrong accessToken").toCompletableFuture()
        }
        if (spotifyUserId != this.spotifyUserId) {
            return RuntimeException("Wrong spotifyUserId").toCompletableFuture()
        }
        if (playlists.none { it.id == playlistId }) {
            return RuntimeException("No playlist with id $playlistId").toCompletableFuture()
        }

        return Future { playlists.first { it.id == playlistId }.tracks }
    }

    override fun createPlaylist(accessToken: AccessToken, spotifyUserId: SpotifyUserId, name: PlaylistName): CompletableFuture<Playlist> {
        if (accessToken != this.accessToken) {
            return RuntimeException("Wrong accessToken").toCompletableFuture()
        }

        val playlist = PlaylistWithTracks(name)
        playlists += playlist
        return Future { playlist.toPlaylist() }
    }

    override fun addTracksToPlaylist(accessToken: AccessToken, spotifyUserId: SpotifyUserId, playlistId: PlaylistId, tracks: List<TrackUri>): CompletableFuture<Unit> {
        if (accessToken != this.accessToken) {
            return RuntimeException("Wrong accessToken").toCompletableFuture()
        }
        if (spotifyUserId != this.spotifyUserId) {
            return RuntimeException("Wrong spotifyUserId").toCompletableFuture()
        }
        if (playlists.none { it.id == playlistId }) {
            return RuntimeException("No playlist with id $playlistId").toCompletableFuture()
        }

        playlists = playlists.map { playlist ->
            if (playlist.id != playlistId) {
                return@map playlist
            }
            return@map playlist.copy(tracks = playlist.tracks + tracks.map { getTrack(it) })
        }
        return Future { }
    }

    fun reset() {
        oAuth2Code = UUID.randomUUID().toString()
        spotifyUserId = SpotifyUserId(UUID.randomUUID().toString())
        accessToken = AccessToken(UUID.randomUUID().toString())
        refreshToken = RefreshToken(UUID.randomUUID().toString())
        playlists = emptyList()
    }
}
