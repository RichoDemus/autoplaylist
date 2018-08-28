package com.richodemus.autoplaylist.playlist

import com.richodemus.autoplaylist.dto.Album
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.Track
import com.richodemus.autoplaylist.spotify.AccessToken
import com.richodemus.autoplaylist.spotify.PlaylistId
import com.richodemus.autoplaylist.spotify.PlaylistName
import com.richodemus.autoplaylist.spotify.SpotifyPort
import kotlinx.coroutines.experimental.launch
import org.slf4j.LoggerFactory

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
        suspend fun create(
                name: PlaylistName,
                artist: ArtistName,
                exclusions: List<String>,
                accessToken: AccessToken,
                spotifyPort: SpotifyPort
        ) = spotifyPort.createPlaylist(accessToken, name)
                .let { Playlist(it.id, it.name, exclusions, artist, accessToken, spotifyPort) }
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun albumsWithTracks(): List<Album> {
        return spotifyPort.findArtist(accessToken, artist)
                .flatMap { spotifyPort.getAlbums(accessToken, it.id) }
    }

    override fun toString(): String {
        return "Playlist $name from artist $artist"
    }

    /**
     * Make sure all tracks are in the spotify playlist
     */
    suspend fun sync(): List<Album> {
        // todo maybe add new tracks to the top of the paylist?
        logger.info("Sync playlist $name to Spotify")

        val actualTracks = spotifyPort.getTracks(accessToken, id)
        val albumsWithTracks = albumsWithTracks()

        launch {
            logger.info("Got {} albums with {} tracks", albumsWithTracks.size, albumsWithTracks.flatMap { it.tracks }.size)
        }

        val expectedTracks = albumsWithTracks.flatMap { it.tracks }
        val expectedTracksDeduplicated = expectedTracks
                .deduplicate()
                .excludeTracks(exclusions)

        launch {
            logger.info("{} tracks remaining after deduplication and filtering", expectedTracksDeduplicated.size)
        }

        val missingTracks = expectedTracksDeduplicated.filterNot { it in actualTracks }

        try {
            spotifyPort.addTracksToPlaylist(accessToken, id, missingTracks.map { it.uri })
            logger.info("Done syncing {} tracks to {}", expectedTracksDeduplicated.size, this)
        } catch (e: Exception) {
            logger.error("Failed to create and fill {}", this, e)
            throw e
        }

        return albumsWithTracks
    }

    // todo make it possible to chose different "deduplicate strategies"
    private fun List<Track>.deduplicate() = this.distinctBy { track -> track.name }

    private fun List<Track>.excludeTracks(exclusions: List<String>) = this.filterNot { track -> track.matches(exclusions) }

    /**
     * Returns true if any of the exclusion strings can be found in the track name
     */
    private fun Track.matches(exclusions: List<String>) = exclusions.any { exclusion ->
        exclusion.toLowerCase() in this.name.value.toLowerCase()
    }
}
