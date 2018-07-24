package com.richodemus.autoplaylist.spotify

import com.richodemus.autoplaylist.dto.Album
import com.richodemus.autoplaylist.dto.ArtistId
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.Track
import com.richodemus.autoplaylist.dto.TrackUri
import io.github.vjames19.futures.jdk8.Future
import io.github.vjames19.futures.jdk8.map
import java.util.concurrent.CompletableFuture
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named
internal class SpotifyAdapter(private val spotifyClient: SpotifyClient) : SpotifyPort {
    override fun getToken(code: String) = spotifyClient.getToken(code)

    override fun getUserId(accessToken: AccessToken) = spotifyClient.getUserId(accessToken)

    override fun getPlaylists(accessToken: AccessToken) = spotifyClient.getPlaylists(accessToken)

    override fun refreshToken(refreshToken: RefreshToken) = spotifyClient.refreshToken(refreshToken)

    override fun findArtist(accessToken: AccessToken, name: ArtistName) = spotifyClient.findArtist(accessToken, name)

    override fun getAlbums(accessToken: AccessToken, artistId: ArtistId): CompletableFuture<List<Album>> {
        return spotifyClient.getAlbums(accessToken, artistId)
                .map { albums ->
                    albums.map { it to spotifyClient.getTracks(accessToken, it.id) }
                            .map { it.first to it.second.join() }
                }
                .map { it.map { it.first to it.second.toDtoTrack() } }
                .map { it.map { Album(it.first.id, it.first.name, it.second) } }
    }

    override fun getTracks(
            accessToken: AccessToken,
            spotifyUserId: SpotifyUserId,
            playlistId: PlayListId
    ) = spotifyClient.getTracks(accessToken, spotifyUserId, playlistId)

    override fun createPlaylist(
            accessToken: AccessToken,
            spotifyUserId: SpotifyUserId,
            name: PlaylistName
    ) = spotifyClient.createPlaylist(accessToken, spotifyUserId, name, "Autocreated", false)

    override fun addTracksToPlaylist(
            accessToken: AccessToken,
            spotifyUserId: SpotifyUserId,
            id: PlayListId,
            tracks: List<TrackUri>
    ) = Future<Unit> {
        tracks.chunked(100)
                .map { spotifyClient.addTracks(accessToken, spotifyUserId, id, it) }.map { it.join() }
                .map { Unit }
    }

    private fun Iterable<com.richodemus.autoplaylist.spotify.Track>.toDtoTrack() = map { Track(it.id, it.name, it.uri) }
}
