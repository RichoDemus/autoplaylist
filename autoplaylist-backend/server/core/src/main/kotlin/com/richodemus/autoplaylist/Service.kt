package com.richodemus.autoplaylist

import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.spotify.AccessToken
import com.richodemus.autoplaylist.spotify.PlayList
import com.richodemus.autoplaylist.spotify.SpotifyPort
import com.richodemus.autoplaylist.user.UserService
import io.github.vjames19.futures.jdk8.Future
import io.github.vjames19.futures.jdk8.flatMap
import io.github.vjames19.futures.jdk8.map
import io.github.vjames19.futures.jdk8.toCompletableFuture
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named
class Service @Inject internal constructor(
        private val userService: UserService,
        private val spotifyPort: SpotifyPort
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun login(oAuthCode: String): CompletableFuture<UserId> {
        logger.info("login: $oAuthCode")
        val tokenFuture = spotifyPort.getToken(oAuthCode)
                .map {
                    logger.info("Tokens: {}", it)
                    it
                }

        val userFuture = tokenFuture.flatMap { getUserIdMemoized(it.accessToken) }
                .flatMap { userService.findOrCreateUser(it) }

        val user = userFuture.join()
        tokenFuture.join().refreshToken?.let {
            user.refreshToken = it
        }

        logger.info("Created user: {}", user)

        return userFuture.map { it.userId }
    }

    fun getSpotifyUserId(userId: UserId): CompletableFuture<SpotifyUserId> {
        val user = userService.getUser(userId) ?: return IllegalStateException("No such user").toCompletableFuture()

        return Future { user.spotifyUserId }
    }

    fun getPlaylists(userId: UserId): CompletableFuture<List<PlayList>> {
        val accessToken = userService.getUser(userId)?.accessToken
                ?: return IllegalStateException("No such user").toCompletableFuture()

        return spotifyPort.getPlaylists(accessToken)
    }

    private val getUserIdMemoized = { accessToken: AccessToken -> spotifyPort.getUserId(accessToken) }.memoize()
}