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
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named
internal class SpotifyAdapter(private val spotifyClient: SpotifyClient) : SpotifyPort {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getToken(code: String) = withRetry { spotifyClient.getToken(code) }

    override fun getUserId(accessToken: AccessToken) = withRetry { spotifyClient.getUserId(accessToken) }

    override fun getPlaylists(accessToken: AccessToken) = withRetry { spotifyClient.getPlaylists(accessToken) }

    override fun refreshToken(refreshToken: RefreshToken) = withRetry { spotifyClient.refreshToken(refreshToken) }

    override fun findArtist(accessToken: AccessToken, name: ArtistName) = withRetry {
        spotifyClient.findArtist(accessToken, name).map { it.map { artist -> artist.toDtoArtist() } }
    }

    override fun getAlbums(accessToken: AccessToken, artistId: ArtistId): CompletableFuture<List<Album>> {
        return withRetry {
            spotifyClient.getAlbums(accessToken, artistId)
                    .map { albums ->
                        albums.map { it to spotifyClient.getTracks(accessToken, it.id) }
                                .map { it.first to it.second.join() }
                    }
                    .map { pair -> pair.map { it.first to it.second.toDtoTrack() } }
                    .map { pair -> pair.map { Album(it.first.id, it.first.name, it.second) } }
        }
    }

    override fun getTracks(
            accessToken: AccessToken,
            spotifyUserId: SpotifyUserId,
            playlistId: PlaylistId
    ) = withRetry { spotifyClient.getTracks(accessToken, spotifyUserId, playlistId) }

    override fun createPlaylist(
            accessToken: AccessToken,
            spotifyUserId: SpotifyUserId,
            name: PlaylistName
    ) = withRetry { spotifyClient.createPlaylist(accessToken, spotifyUserId, name, "Autocreated", false) }

    override fun addTracksToPlaylist(
            accessToken: AccessToken,
            spotifyUserId: SpotifyUserId,
            id: PlaylistId,
            tracks: List<TrackUri>
    ) = Future<Unit> {
        tracks.chunked(100)
                .map { withRetry { spotifyClient.addTracks(accessToken, spotifyUserId, id, it) } }.map { it.join() }
                .map { Unit }
    }

    private fun Iterable<com.richodemus.autoplaylist.spotify.Track>.toDtoTrack() = map { Track(it.id, it.name, it.uri) }

    /**
     * Wraps a future invokation in retry logic
     */
    private fun <T> withRetry(function: () -> CompletableFuture<T>): CompletableFuture<T> {
        return Future {
            for (i in 1..10) {
                try {
                    return@Future function().join()

                } catch (e: Exception) {
                    val exception = e.cause
                    if (exception is RateLimitExceededException) {
                        logger.warn("Rate Limit Exceeded, sleeping for ${exception.retryAfter} seconds")
                        Thread.sleep(exception.retryAfter * 1000 + 1000) //lets sleep an extra second
                    } else {
                        logger.warn("Failed call: ${e.message}, retrying", e)
                        Thread.sleep(10L)
                    }
                }
            }
            throw RuntimeException("Call $function failed after 10 attempts")
        }
    }

    private fun Artist.toDtoArtist() = com.richodemus.autoplaylist.dto.Artist(this.id, this.name)
}
