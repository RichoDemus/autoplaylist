package com.richodemus.autoplaylist.playlist

import com.richodemus.autoplaylist.dto.Album
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.Track
import com.richodemus.autoplaylist.flatten
import com.richodemus.autoplaylist.spotify.AccessToken
import com.richodemus.autoplaylist.spotify.PlaylistId
import com.richodemus.autoplaylist.spotify.PlaylistName
import com.richodemus.autoplaylist.spotify.SpotifyPort
import io.github.vjames19.futures.jdk8.flatMap
import io.github.vjames19.futures.jdk8.map
import io.github.vjames19.futures.jdk8.onFailure
import io.github.vjames19.futures.jdk8.onSuccess
import io.github.vjames19.futures.jdk8.zip
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

/**
 * Not supposed to contain tracks (or?)
 * supposed to describe what a playlist is supposed to contain
 */
class Playlist private constructor(
        val id: PlaylistId,
        val name: PlaylistName,
        val exclusions: List<String>,
        private val artist: ArtistName,
        private val accessToken: AccessToken,
        private val spotifyPort: SpotifyPort
) {
    companion object {
        fun create(
                name: PlaylistName,
                artist: ArtistName,
                exclusions: List<String>,
                accessToken: AccessToken,
                spotifyPort: SpotifyPort
        ) = spotifyPort.createPlaylist(accessToken, name)
                .map { Playlist(it.id, it.name, exclusions, artist, accessToken, spotifyPort) }
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    fun albumsWithTracks(): CompletableFuture<List<Album>> {
        return spotifyPort.findArtist(accessToken, artist)
                .flatMap { artistIds ->
                    artistIds.map { spotifyPort.getAlbums(accessToken, it.id) }.flatten()
                }.map { albums -> albums.flatMap { it } }
    }

    override fun toString(): String {
        return "Playlist $name from artist $artist"
    }

    /**
     * Make sure all tracks are in the spotify playlist
     */
    fun sync(): CompletableFuture<List<Album>> {
        // todo maybe add new tracks to the top of the paylist?
        logger.info("Sync playlist $name to Spotify")

        val actualTracksFuture = spotifyPort.getTracks(accessToken, id)
        val albumsWithTracksFuture = albumsWithTracks()
        albumsWithTracksFuture.onSuccess { albums ->
            logger.info("Got {} albums with {} tracks", albums.size, albums.flatMap { it.tracks }.size)
        }

        val expectedTracksFuture = albumsWithTracksFuture.map { albums -> albums.flatMap { it.tracks } }
        val expectedTracksDeduplicatedFuture = expectedTracksFuture
                .deduplicate()
                .excludeTracks(exclusions)
        expectedTracksDeduplicatedFuture.onSuccess {
            logger.info("{} tracks remaining after deduplication and filtering", it.size)
        }

        val missingTracksFuture = actualTracksFuture.zip(expectedTracksDeduplicatedFuture) { actual, expected ->
            expected.filterNot { it in actual }
        }

        val addTracksToPlaylistFuture = missingTracksFuture.flatMap { tracks ->
            spotifyPort.addTracksToPlaylist(accessToken, id, tracks.map { it.uri })
        }

        addTracksToPlaylistFuture.onSuccess {
            logger.info("Done syncing {} tracks to {}", expectedTracksDeduplicatedFuture.join().size, this)
        }
        addTracksToPlaylistFuture.onFailure { logger.error("Failed to create and fill {}", this, it) }

        return albumsWithTracksFuture
    }

    // todo make it possible to chose different "deduplicate strategies"
    private fun CompletableFuture<List<Track>>.deduplicate() = this.map { it.distinctBy { track -> track.name } }

    private fun CompletableFuture<List<Track>>.excludeTracks(exclusions: List<String>) = this.map { tracks ->
        tracks.filterNot { track -> track.matches(exclusions) }
    }

    /**
     * Returns true if any of the exclusion strings can be found in the track name
     */
    private fun Track.matches(exclusions: List<String>) = exclusions.any { exclusion ->
        exclusion.toLowerCase() in this.name.value.toLowerCase()
    }
}
