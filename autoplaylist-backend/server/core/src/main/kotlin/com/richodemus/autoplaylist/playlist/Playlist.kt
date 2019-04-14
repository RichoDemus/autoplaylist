package com.richodemus.autoplaylist.playlist

import com.richodemus.autoplaylist.dto.Album
import com.richodemus.autoplaylist.dto.Exclusion
import com.richodemus.autoplaylist.dto.PlaylistId
import com.richodemus.autoplaylist.dto.PlaylistName
import com.richodemus.autoplaylist.dto.Rules
import com.richodemus.autoplaylist.dto.SpotifyPlaylistId
import com.richodemus.autoplaylist.dto.Track
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.dto.events.PlaylistRulesChanged
import com.richodemus.autoplaylist.dto.events.PlaylistSyncChanged
import com.richodemus.autoplaylist.event.EventStore
import com.richodemus.autoplaylist.spotify.AccessToken
import com.richodemus.autoplaylist.spotify.SpotifyService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Not supposed to contain tracks (or?)
 * supposed to describe what a playlist is supposed to contain
 */
class Playlist(
        val id: PlaylistId,
        val userId:  UserId,
        val name: PlaylistName,
        private val spotifyPort: SpotifyService,
        private val eventStore: EventStore
) : CoroutineScope {
    private val logger = LoggerFactory.getLogger(javaClass)
    override val coroutineContext = Dispatchers.Default

    var spotifyPlaylistId: SpotifyPlaylistId? = null
        private set

    var rules: Rules = Rules()
        private set

    var sync = false
        private set

    var lastSynced: ZonedDateTime = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC"))
        private set

    private var tracksFromRules: List<Album> = emptyList()

    init {
        eventStore.consume { event ->
            when (event) {
                is PlaylistRulesChanged -> if (event.playlistId == id) overwriteRules(event)
                is PlaylistSyncChanged -> if (event.playlistId == id) sync = event.sync
                else -> logger.debug("Event of type: ${event.type()} not handled")
            }
        }
    }

    override fun toString(): String {
//        return "Playlist $name from artist $artist"
        return "Playlist $name"
    }

    /**
     * Make sure all tracks are in the spotify playlist
     */
    suspend fun sync(): List<Album> {
        if (spotifyPlaylistId == null) {
            val (generatedId, _) = spotifyPort.createPlaylist(userId, name)
            spotifyPlaylistId = generatedId
        }

        val spotifyPlaylistId = this.spotifyPlaylistId
                ?: throw IllegalStateException("SpotifyPlaylistId shouldn't be null here")


        // todo maybe add new tracks to the top of the paylist?
        logger.info("Sync playlist $name to Spotify")

        // todo I think we can do these in parallel?
        val actualTracks = spotifyPort.getTracks(userId,spotifyPlaylistId)
        val albumsWithTracks = rules.artists.flatMap { spotifyPort.getAlbums(userId,it) }

        launch {
            logger.info("Got {} albums with {} tracks", albumsWithTracks.size, albumsWithTracks.flatMap { it.tracks }.size)
        }

        val expectedTracks = albumsWithTracks.flatMap { it.tracks }
        val expectedTracksDeduplicated = expectedTracks
                .deduplicate()
                .excludeTracks(rules.exclusions)

        launch {
            logger.info("{} tracks remaining after deduplication and filtering", expectedTracksDeduplicated.size)
        }

        val missingTracks = expectedTracksDeduplicated.filterNot { it in actualTracks }

        try {
            spotifyPort.addTracksToPlaylist(userId,spotifyPlaylistId, missingTracks.map { it.uri })
            logger.info("Done syncing {} tracks to {}", expectedTracksDeduplicated.size, this)
        } catch (e: Exception) {
            logger.error("Failed to create and fill {}", this, e)
            throw e
        }

        lastSynced = ZonedDateTime.now(ZoneId.of("UTC"))
        return albumsWithTracks
    }

    // todo make it possible to chose different "deduplicate strategies"
    @JvmName("deduplicateTracks")
    private fun List<Track>.deduplicate() = this.distinctBy { track -> track.name }

    private fun List<Album>.deduplicate() =
            this.flatMap { it.tracks.map { track -> it to track } }
                    .distinctBy { it.second.name }
                    .groupBy { it.first }
                    .mapValues { entry -> entry.value.map { it.second } }
                    .map { it.key.copy(tracks = it.value) }

    @JvmName("excludeTracksList")
    private fun List<Track>.excludeTracks(exclusions: List<Exclusion>) =
            this.filterNot { track -> track.matches(exclusions.map { it.keyword.value }) }

    private fun List<Album>.excludeTracks(exclusions: List<Exclusion>) =
            this.map { album ->
                album.copy(tracks = album.tracks.filterNot { track ->
                    track.matches(exclusions.map { it.keyword.value })
                })
            }

    /**
     * Returns true if any of the exclusion strings can be found in the track name
     */
    private fun Track.matches(keyword: List<String>) = keyword.any { exclusion ->
        exclusion.toLowerCase() in this.name.value.toLowerCase()
    }

    fun overwriteRules(rules: Rules) {
        val event = PlaylistRulesChanged(
                playlistId = id,
                rules = rules
        )
        eventStore.produce(event)
        overwriteRules(event)
    }

    private fun overwriteRules(playlistRulesChanged: PlaylistRulesChanged) {
        if (this.rules == playlistRulesChanged.rules) {
            return
        }
        this.rules = playlistRulesChanged.rules
        tracksFromRules = emptyList()
    }

    // todo this should be part of the rules domain, maybe
    @Synchronized
    suspend fun tracksFromRules(): List<Album> {
        if (tracksFromRules.isEmpty() && rules.artists.isNotEmpty()) {
            tracksFromRules = rules.artists.flatMap { spotifyPort.getAlbums(userId,it) }
                    .deduplicate()
                    .excludeTracks(rules.exclusions)
        }
        return tracksFromRules
    }

    suspend fun startSyncinc() = changeSync(true)

    suspend fun stopSyncing() = changeSync(false)

    private suspend fun changeSync(enabled: Boolean) {
        if (sync != enabled) {
            eventStore.produce(PlaylistSyncChanged(
                    playlistId = id,
                    sync = enabled
            ))
            sync()
        }
    }
}
