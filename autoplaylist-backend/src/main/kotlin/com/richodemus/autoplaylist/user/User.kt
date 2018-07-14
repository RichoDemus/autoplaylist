package com.richodemus.autoplaylist.user

import com.richodemus.autoplaylist.AccessToken
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.getAccessToken
import java.time.Duration
import java.time.Instant

internal class User(
        val userId: UserId,
        val spotifyUserId: SpotifyUserId,
        var refreshToken: RefreshToken? = null,
        accessToken: AccessToken? = null,
        private var tokenExpiration: Instant = Instant.MIN
) {
    var accessToken = accessToken
        get() {
            renewAccessToken()
            return field
        }
        private set

    @Synchronized
    private fun renewAccessToken() {
        if (Instant.now().isAfter(tokenExpiration)) {
            refreshToken?.let {
                val (accessToken1, _, _, expiresIn, _) = getAccessToken(it).join()
                accessToken = accessToken1
                tokenExpiration = Instant.now().plus(Duration.ofSeconds(expiresIn.toLong()))
            }
        }
    }
}
