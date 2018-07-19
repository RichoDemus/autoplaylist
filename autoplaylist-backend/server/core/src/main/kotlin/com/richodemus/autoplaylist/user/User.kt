package com.richodemus.autoplaylist.user

import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.event.EventStore
import com.richodemus.autoplaylist.event.RefreshTokenUpdated
import com.richodemus.autoplaylist.spotify.AccessToken
import com.richodemus.autoplaylist.spotify.SpotifyPort
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
            renewAccessToken()
            return field
        }
        private set

    internal var refreshToken = refreshToken
        internal set(value) {
            field = value
            eventStore.produce(RefreshTokenUpdated(userId = userId, refreshToken = value))
        }

    @Synchronized
    private fun renewAccessToken() {
        if (Instant.now().isAfter(tokenExpiration)) {
            val (accessToken1, _, _, expiresIn, _) = spotifyPort.refreshToken(refreshToken).join()
            accessToken = accessToken1
            tokenExpiration = Instant.now().plus(Duration.ofSeconds(expiresIn.toLong()))
        }
    }

    override fun toString(): String {
        return "User(userId=$userId, spotifyUserId=$spotifyUserId)"
    }
}
