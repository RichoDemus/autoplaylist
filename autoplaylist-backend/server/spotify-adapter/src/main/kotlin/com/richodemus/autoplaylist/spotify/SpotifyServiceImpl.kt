package com.richodemus.autoplaylist.spotify;

import com.richodemus.autoplaylist.dto.Album
import com.richodemus.autoplaylist.dto.Artist
import com.richodemus.autoplaylist.dto.ArtistId
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.PlaylistName
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyPlaylistId
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.Track
import com.richodemus.autoplaylist.dto.TrackUri
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.dto.events.RefreshTokenUpdated
import com.richodemus.autoplaylist.event.EventStore
import com.richodemus.autoplaylist.usermapping.UserIdMappingService
import org.slf4j.LoggerFactory
import javax.inject.Named;
import javax.inject.Singleton;


@Singleton
@Named
class SpotifyServiceImpl(
        private val spotifyPort: SpotifyPort,
        private val userIdMappingService: UserIdMappingService,
        private val eventStore: EventStore
) : SpotifyService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var refreshTokens = emptyMap<UserId, List<RefreshToken>>()
    private var accessTokens = emptyMap<UserId, AccessToken>()

    init {
        eventStore.consume { event ->
            if (event is RefreshTokenUpdated) {
                addRefreshToken(event.userId, event.refreshToken)
            }
        }
    }

    private fun addRefreshToken(userId: UserId, refreshToken: RefreshToken) {
        val tokens = refreshTokens.getOrElse(userId) { emptyList() }
        refreshTokens = refreshTokens + Pair(userId, tokens + refreshToken)
    }

    override suspend fun getUserId(code: String): UserId {
        val tokens = spotifyPort.getToken(code)
        val spotifyUserId = getUserId(tokens.accessToken)
        val userId = userIdMappingService.getUserId(spotifyUserId)
        val refreshToken = tokens.refreshToken ?: throw IllegalStateException("No refresh token for user $userId")
        eventStore.produce(RefreshTokenUpdated(userId = userId, refreshToken = refreshToken))
        accessTokens = accessTokens.plus(Pair(userId, tokens.accessToken))
        return userId
    }

    override suspend fun getUserId(userId: UserId): SpotifyUserId = spotifyPort.getUserId(getAccessToken(userId))
    private suspend fun getUserId(accessToken: AccessToken): SpotifyUserId = spotifyPort.getUserId(accessToken)
    override suspend fun getPlaylists(userId: UserId): List<Playlist> = spotifyPort.getPlaylists(getAccessToken(userId))
    override suspend fun findArtist(userId: UserId, name: ArtistName): List<Artist> = spotifyPort.findArtist(getAccessToken(userId), name)

    override suspend fun getArtist(userId: UserId, artistId: ArtistId): Artist? = spotifyPort.getArtist(getAccessToken(userId), artistId)
    override suspend fun getAlbums(userId: UserId, artistId: ArtistId): List<Album> = spotifyPort.getAlbums(getAccessToken(userId), artistId)
    override suspend fun getTracks(
            userId: UserId,
            playlistId: SpotifyPlaylistId
    ): List<Track> = spotifyPort.getTracks(getAccessToken(userId), playlistId)

    override suspend fun createPlaylist(
            userId: UserId,
            name: PlaylistName
    ): Playlist = spotifyPort.createPlaylist(getAccessToken(userId), name)

    override suspend fun addTracksToPlaylist(
            userId: UserId,
            playlistId: SpotifyPlaylistId,
            tracks: List<TrackUri>
    ) = spotifyPort.addTracksToPlaylist(getAccessToken(userId), playlistId, tracks)

    // todo try all valid refresh tokens and invalidate if they fail x times
    // todo better synchronization
    @Synchronized
    private suspend fun getAccessToken(userId: UserId): AccessToken {
        var accessToken = accessTokens[userId]
        if (accessToken == null) {
            accessToken = spotifyPort.refreshToken(getRefreshToken(userId)).accessToken
            accessTokens = accessTokens.plus(Pair(userId, accessToken))
        }
        return accessToken
    }

    // todo test all tokens or something
    private fun getRefreshToken(userId: UserId): RefreshToken =
            refreshTokens[userId]?.lastOrNull() ?: throw IllegalStateException("No refresh token for $userId")
}
