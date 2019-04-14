package com.richodemus.autoplaylist.user

import com.richodemus.autoplaylist.dto.PlaylistId
import com.richodemus.autoplaylist.dto.PlaylistName
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.dto.events.Event
import com.richodemus.autoplaylist.dto.events.PlaylistCreated
import com.richodemus.autoplaylist.dto.events.RefreshTokenUpdated
import com.richodemus.autoplaylist.event.EventStore
import com.richodemus.autoplaylist.playlist.Playlist
import com.richodemus.autoplaylist.spotify.AccessToken
import com.richodemus.autoplaylist.spotify.SpotifyPort
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.time.Instant

class User internal constructor(
        private val eventStore: EventStore,
        private val spotifyPort: SpotifyPort,
        val userId: UserId,
        val spotifyUserId: SpotifyUserId,
        refreshToken: RefreshToken,
        accessToken: AccessToken? = null,
        private var tokenExpiration: Instant = Instant.MIN
) {
    var accessToken = accessToken
        get() {
            // possible to suspend get method?
            runBlocking { renewAccessToken() }
            return field
        }
        private set

    internal var refreshToken = refreshToken
        internal set(value) {
            field = value
            eventStore.produce(RefreshTokenUpdated(userId = userId, refreshToken = value))
        }

    private var playlists = emptyList<Playlist>()

    @Synchronized
    private suspend fun renewAccessToken() {
        if (Instant.now().isAfter(tokenExpiration)) {
            // todo proper error handling if refreshing fails
            val (accessToken1, _, _, expiresIn, _) = spotifyPort.refreshToken(refreshToken)
            accessToken = accessToken1
            tokenExpiration = Instant.now().plus(Duration.ofSeconds(expiresIn.toLong()))
        }
    }


    fun process(event: Event) {
        when (event) {
            is PlaylistCreated -> createPlaylist(event)
        }
    }

    fun createPlaylist(name: PlaylistName): Playlist {
        val event = PlaylistCreated(userId = userId, playlistName = name)

        eventStore.produce(event)
        return createPlaylist(event)
    }

    // todo remove once this is CQRS:ed
    @Synchronized
    private fun createPlaylist(event: PlaylistCreated): Playlist {
        if (playlists.any { it.id == event.playlistId }) {
            return playlists.first { it.id == event.playlistId }
        }
        // todo don't use accessToken!!!
        val playlist = Playlist(event.playlistId, event.playlistName, accessToken!!, spotifyPort, eventStore)
        playlists = playlists.plus(playlist)
        return playlist
    }

    override fun toString(): String {
        return "User(userId=$userId, spotifyUserId=$spotifyUserId)"
    }

    fun getPlaylist(playlistId: PlaylistId): Playlist {
        return playlists.first { it.id == playlistId }
    }

    fun getPlaylists() = playlists
}
