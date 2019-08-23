package com.richodemus.autoplaylist.spotify

import com.richodemus.autoplaylist.dto.Album
import com.richodemus.autoplaylist.dto.ArtistId
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.PlaylistName
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyPlaylistId
import com.richodemus.autoplaylist.dto.Track
import com.richodemus.autoplaylist.dto.TrackUri
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named
internal class SpotifyAdapter(private val spotifyClient: SpotifyClient) : SpotifyPort {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun getToken(code: String) = withRetry { spotifyClient.getTokens(code) }

    override suspend fun getUserId(accessToken: AccessToken) = withRetry { spotifyClient.getUserId(accessToken) }

    override suspend fun getPlaylists(accessToken: AccessToken) = withRetry { spotifyClient.getPlaylists(accessToken) }

    override suspend fun refreshToken(refreshToken: RefreshToken) = withRetry { spotifyClient.refreshToken(refreshToken) }

    override suspend fun findArtist(accessToken: AccessToken, name: ArtistName) = withRetry {
        spotifyClient.findArtist(accessToken, name).map { artist -> artist.toDtoArtist() }
    }

    override suspend fun getArtist(accessToken: AccessToken, artistId: ArtistId) = withRetry {
        spotifyClient.getArtist(accessToken, artistId)?.toDtoArtist()
    }

    override suspend fun getAlbums(accessToken: AccessToken, artistId: ArtistId): List<Album> {
        return withRetry {
            spotifyClient.getAlbums(accessToken, artistId)
                    .map { it to spotifyClient.getTracks(accessToken, it.id) }
                    .map { it.first to it.second.toDtoTrack() }
                    .map { Album(it.first.id, it.first.name, it.second) }
        }
    }

    override suspend fun getTracks(
            accessToken: AccessToken,
            playlistId: SpotifyPlaylistId
    ) = withRetry {
        spotifyClient.getTracks(accessToken, playlistId)
                .map { Track(it.id, it.name, it.uri) }
    }

    override suspend fun createPlaylist(
            accessToken: AccessToken,
            name: PlaylistName
    ) = withRetry { spotifyClient.createPlaylist(accessToken, name, "Autocreated", false) }

    override suspend fun addTracksToPlaylist(
            accessToken: AccessToken,
            playlistId: SpotifyPlaylistId,
            tracks: List<TrackUri>
    ) = tracks.chunked(100)
            .map { withRetry { spotifyClient.addTracks(accessToken, playlistId, it) } }
            .let { Unit }


    private fun Iterable<com.richodemus.autoplaylist.spotify.Track>.toDtoTrack() = map { Track(it.id, it.name, it.uri) }

    /**
     * Wraps a suspended function in retry logic
     */
    private suspend fun <T> withRetry(function: suspend () -> T): T {
        for (i in 1..10) {
            try {
                return function()

            } catch (e: Exception) {
                val exception = e.cause
                if (exception is RateLimitExceededException) {
                    logger.warn("Rate Limit Exceeded, sleeping for ${exception.retryAfter} seconds")
                    delay(exception.retryAfter * 1000 + 1000) //lets sleep an extra second
                } else {
                    logger.warn("Failed call: ${e.message}, retrying", e)
                    delay(10L)
                }
            }
        }
        throw RuntimeException("Call $function failed after 10 attempts")
    }

    private fun Artist.toDtoArtist() = com.richodemus.autoplaylist.dto.Artist(this.id, this.name)
}
