package com.richodemus.autoplaylist.spotify

import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import java.util.concurrent.CompletableFuture

interface SpotifyPort {
    fun getToken(code: String): CompletableFuture<Tokens>
    fun getUserId(accessToken: AccessToken): CompletableFuture<SpotifyUserId>
    fun getPlaylists(accessToken: AccessToken): CompletableFuture<List<PlayList>>
    fun refreshToken(refreshToken: RefreshToken): CompletableFuture<Tokens>
}
