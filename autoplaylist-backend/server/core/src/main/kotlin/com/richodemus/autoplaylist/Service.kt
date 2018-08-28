package com.richodemus.autoplaylist

import com.richodemus.autoplaylist.dto.Artist
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.Track
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.playlist.PlaylistWithAlbums
import com.richodemus.autoplaylist.spotify.AccessToken
import com.richodemus.autoplaylist.spotify.Playlist
import com.richodemus.autoplaylist.spotify.PlaylistId
import com.richodemus.autoplaylist.spotify.PlaylistName
import com.richodemus.autoplaylist.spotify.SpotifyPort
import com.richodemus.autoplaylist.user.UserService
import kotlinx.coroutines.experimental.launch
import org.slf4j.LoggerFactory
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

    suspend fun login(oAuthCode: String): UserId {
        // todo refactor this method....
        logger.info("login: $oAuthCode")
        val tokens = spotifyPort.getToken(oAuthCode)

        val userId = getGetUserIdMemoized(tokens.accessToken)

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

    suspend fun getPlaylists(userId: UserId): List<Playlist> {
        val accessToken = userService.getUser(userId)?.accessToken
                ?: throw IllegalStateException("No such user")

        return spotifyPort.getPlaylists(accessToken)
    }

    suspend fun getPlaylist(userId: UserId, playlistId: PlaylistId): List<Track> {
        val accessToken = userService.getUser(userId)?.accessToken
                ?: throw IllegalStateException("No such user")

        return spotifyPort.getTracks(accessToken, playlistId)
    }

    suspend fun createPlaylist(
            userId: UserId,
            name: PlaylistName,
            artist: ArtistName,
            exclusions: List<String>
    ): PlaylistWithAlbums {
        val user = userService.getUser(userId)
                ?: throw IllegalStateException("No user with id $userId")

        val playlist = user.createPlaylist(name, artist, exclusions)
        launch {
            logger.error("User {} failed to create playlist named {} from artist {}", arrayOf(user, name, artist))
        }
        val playlistContents = playlist.sync()

        return PlaylistWithAlbums(playlist.id, playlistContents)
    }

    suspend fun findArtists(userId: UserId, artistName: ArtistName): List<Artist> {
        val accessToken = userService.getUser(userId)?.accessToken
                ?: throw IllegalStateException("No such user")

        return spotifyPort.findArtist(accessToken, artistName)
    }

    private suspend fun getGetUserIdMemoized(accessToken: AccessToken) = spotifyPort.getUserId(accessToken)
}
