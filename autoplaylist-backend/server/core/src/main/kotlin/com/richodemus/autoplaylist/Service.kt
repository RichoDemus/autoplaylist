package com.richodemus.autoplaylist

import com.richodemus.autoplaylist.dto.Artist
import com.richodemus.autoplaylist.dto.ArtistId
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.PlaylistName
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.spotify.AccessToken
import com.richodemus.autoplaylist.spotify.SpotifyPort
import com.richodemus.autoplaylist.user.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.slf4j.LoggerFactory
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named
class Service(
        private val userService: UserService,
        private val spotifyPort: SpotifyPort
) : CoroutineScope {
    override val coroutineContext = Dispatchers.Default
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun login(oAuthCode: String): UserId {
        // todo refactor this method....
        logger.info("login: $oAuthCode")
        val tokens = spotifyPort.getToken(oAuthCode)
        logger.info("Got tokens from spotify")
        val userId = getGetUserIdMemoized(tokens.accessToken)
        logger.info("UserId is {}", userId)
        val refreshToken = tokens.refreshToken
                ?: throw IllegalStateException("No refresh token for user $userId")

        val user = userService.findOrCreateUser(userId, refreshToken)

        logger.info("Logged in user: {}", user)

        return user.userId
    }

    fun getSpotifyUserId(userId: UserId): SpotifyUserId {
        val user = userService.getUser(userId) ?: throw IllegalStateException("No such user")

        return user.spotifyUserId
    }

    fun getPlaylists(userId: UserId): List<com.richodemus.autoplaylist.playlist.Playlist> {
        val user = userService.getUser(userId)
        user?.accessToken ?: throw IllegalStateException("No such user")

        return user.getPlaylists()
    }

    fun createPlaylist(
            userId: UserId,
            name: PlaylistName
    ): com.richodemus.autoplaylist.playlist.Playlist {
        val user = userService.getUser(userId)
                ?: throw IllegalStateException("No user with id $userId")

        return user.createPlaylist(name)
    }

    suspend fun findArtists(userId: UserId, artistName: ArtistName): List<Artist> {
        val accessToken = userService.getUser(userId)?.accessToken
                ?: throw IllegalStateException("No such user")

        return spotifyPort.findArtist(accessToken, artistName)
    }

    suspend fun getArtist(userId: UserId, artistId: ArtistId): Artist? {
        val accessToken = userService.getUser(userId)?.accessToken
                ?: throw IllegalStateException("No such user")

        return spotifyPort.getArtist(accessToken, artistId)
    }

    private suspend fun getGetUserIdMemoized(accessToken: AccessToken) = spotifyPort.getUserId(accessToken)
}
