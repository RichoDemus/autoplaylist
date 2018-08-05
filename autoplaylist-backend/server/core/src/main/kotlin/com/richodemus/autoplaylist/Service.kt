package com.richodemus.autoplaylist

import com.richodemus.autoplaylist.dto.Artist
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.playlist.PlaylistWithAlbums
import com.richodemus.autoplaylist.spotify.AccessToken
import com.richodemus.autoplaylist.spotify.Playlist
import com.richodemus.autoplaylist.spotify.PlaylistName
import com.richodemus.autoplaylist.spotify.SpotifyPort
import com.richodemus.autoplaylist.user.UserService
import io.github.vjames19.futures.jdk8.Future
import io.github.vjames19.futures.jdk8.flatMap
import io.github.vjames19.futures.jdk8.map
import io.github.vjames19.futures.jdk8.onFailure
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
        // todo refactor this method....
        logger.info("login: $oAuthCode")
        val tokenFuture = spotifyPort.getToken(oAuthCode)
                .map {
                    logger.info("Tokens: {}", it)
                    it
                }

        val userIdFuture = tokenFuture.flatMap { getUserIdMemoized(it.accessToken) }

        val tokens = tokenFuture.join()
        val refreshToken = tokens.refreshToken
                ?: return IllegalStateException("No refresh token for user ${userIdFuture.join()}").toCompletableFuture()
        val userFuture = userIdFuture
                .flatMap { userService.findOrCreateUser(it, refreshToken) }

        val user = userFuture.join()

        logger.info("Logged in user: {}", user)

        return userFuture.map { it.userId }
    }

    fun getSpotifyUserId(userId: UserId): CompletableFuture<SpotifyUserId> {
        val user = userService.getUser(userId) ?: return IllegalStateException("No such user").toCompletableFuture()

        return Future { user.spotifyUserId }
    }

    fun getPlaylists(userId: UserId): CompletableFuture<List<Playlist>> {
        val accessToken = userService.getUser(userId)?.accessToken
                ?: return IllegalStateException("No such user").toCompletableFuture()

        return spotifyPort.getPlaylists(accessToken)
    }

    fun createPlaylist(userId: UserId, name: PlaylistName, artist: ArtistName): CompletableFuture<PlaylistWithAlbums> {
        val user = userService.getUser(userId)
                ?: return IllegalStateException("No user with id $userId").toCompletableFuture()

        val playlist = user.createPlaylist(name, artist)
        playlist.onFailure {
            logger.error("User {} failed to create playlist named {} from artist {}", arrayOf(user, name, artist))
        }
        val playlistContents = playlist.flatMap { it.sync() }
        return playlistContents.map { PlaylistWithAlbums(it) }
    }

    fun findArtists(userId: UserId, artistName: ArtistName): CompletableFuture<List<Artist>> {
        val accessToken = userService.getUser(userId)?.accessToken
                ?: return IllegalStateException("No such user").toCompletableFuture()

        return spotifyPort.findArtist(accessToken, artistName)
    }

    private val getUserIdMemoized = { accessToken: AccessToken -> spotifyPort.getUserId(accessToken) }.memoize()
}
