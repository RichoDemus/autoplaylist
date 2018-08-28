package com.richodemus.autoplaylist.user

import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.eventstore.EventStore
import com.richodemus.autoplaylist.eventstore.RefreshTokenUpdated
import com.richodemus.autoplaylist.playlist.Playlist
import com.richodemus.autoplaylist.spotify.AccessToken
import com.richodemus.autoplaylist.spotify.PlaylistName
import com.richodemus.autoplaylist.spotify.SpotifyPort
import kotlinx.coroutines.experimental.runBlocking
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


    suspend fun createPlaylist(
            name: PlaylistName,
            artist: ArtistName,
            exclusions: List<String>
    ): Playlist {
        // todo don't use accessToken!!!
        val playlist = Playlist.create(name, artist, exclusions, accessToken!!, spotifyPort)
        playlists = playlists.plus(playlist)
        return playlist
    }

    override fun toString(): String {
        return "User(userId=$userId, spotifyUserId=$spotifyUserId)"
    }
}
