package com.richodemus.autoplaylist

import com.richodemus.autoplaylist.dto.Artist
import com.richodemus.autoplaylist.dto.ArtistId
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.PlaylistName
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.spotify.AccessToken
import com.richodemus.autoplaylist.spotify.SpotifyService
import com.richodemus.autoplaylist.user.UserService
import com.richodemus.autoplaylist.usermapping.UserIdMappingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.slf4j.LoggerFactory
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named
class Service(
        private val userService: UserService,
        private val spotifyPort: SpotifyService,
        private val userIdMappingService: UserIdMappingService
) : CoroutineScope {
    override val coroutineContext = Dispatchers.Default
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun login(oAuthCode: String): UserId {
        // todo refactor this method....
        logger.info("login: $oAuthCode")
//        val tokens = spotifyPort.getToken(oAuthCode)
        val userId = spotifyPort.getUserId(oAuthCode)
        logger.info("Got tokens from spotify")
//        val spotifyUserId = getGetUserIdMemoized(tokens.accessToken)
//        logger.info("UserId is {}", spotifyUserId)
//        val refreshToken = tokens.refreshToken
//                ?: throw IllegalStateException("No refresh token for user $spotifyUserId")

        val user = userService.findOrCreateUser(userId)

        logger.info("Logged in user: {}", user)

        return user.userId
    }

    fun getSpotifyUserId(userId: UserId): SpotifyUserId? {
        return userIdMappingService.getUserId(userId)
    }

    fun getPlaylists(userId: UserId): List<com.richodemus.autoplaylist.playlist.Playlist> {
        val user = userService.getUser(userId)

        return user!!.getPlaylists()
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
        return spotifyPort.findArtist(userId, artistName)
    }

    suspend fun getArtist(userId: UserId, artistId: ArtistId): Artist? {
        return spotifyPort.getArtist(userId, artistId)
    }
}
