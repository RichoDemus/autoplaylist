package com.richodemus.autoplaylist.playlist

import com.richodemus.autoplaylist.dto.Album
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.flatten
import com.richodemus.autoplaylist.spotify.AccessToken
import com.richodemus.autoplaylist.spotify.PlayListId
import com.richodemus.autoplaylist.spotify.PlaylistName
import com.richodemus.autoplaylist.spotify.SpotifyPort
import io.github.vjames19.futures.jdk8.flatMap
import io.github.vjames19.futures.jdk8.map
import io.github.vjames19.futures.jdk8.toCompletableFuture
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

/**
 * Not supposed to contain tracks (or?)
 * supposed to describe what a playlist is supposed to contain
 */
class Playlist private constructor(
        val id: PlayListId,
        private val spotifyUserId: SpotifyUserId,
        val name: PlaylistName,
        private val artist: ArtistName,
        private val accessToken: AccessToken,
        private val spotifyPort: SpotifyPort
) {
    companion object {
        fun create(
                name: PlaylistName,
                spotifyUserId: SpotifyUserId,
                artist: ArtistName,
                accessToken: AccessToken,
                spotifyPort: SpotifyPort
        ) = spotifyPort.createPlaylist(accessToken, spotifyUserId, name)
                .map { Playlist(it.id, spotifyUserId, it.name, artist, accessToken, spotifyPort) }
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    fun albumsWithTracks(): CompletableFuture<List<Album>> {
        return spotifyPort.findArtist(accessToken, artist)
                .flatMap { artistIds ->
                    artistIds.map { spotifyPort.getAlbums(accessToken, it) }.flatten()
                }.map { it.flatMap { it } }
    }

    override fun toString(): String {
        return "Playlist $name from artist $artist"
    }

    /**
     * Make sure all tracks are in the spotify playlist
     */
    fun sync(): CompletableFuture<List<Album>> {
        // todo maybe add new tracks to the top of the paylist?
        // todo rewrite this function
        try {
            logger.info("Sync playlist $name to Spotify")

            val actualTracksFuture = spotifyPort.getTracks(accessToken, spotifyUserId, id)
            val albumsWithTracksFuture = albumsWithTracks()

            val expectedTracks = albumsWithTracksFuture.join().flatMap { it.tracks }
            val actualTracks = actualTracksFuture.join()

            val missingTracks = expectedTracks.filterNot { it.id in actualTracks }
            val addTracksToPlaylistFuture = spotifyPort.addTracksToPlaylist(accessToken, spotifyUserId, id, missingTracks
                    .map { it.uri })
            addTracksToPlaylistFuture
                    .map { logger.info("Done syncing {}", this) }
            // to catch the exception or something
            addTracksToPlaylistFuture.join()
            return albumsWithTracksFuture
        } catch (e: Exception) {
            logger.error("Failed to create and fill {}", this, e)
            return RuntimeException("Failed to create and fill $this", e).toCompletableFuture()
        }
    }
}
