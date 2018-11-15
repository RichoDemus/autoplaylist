package com.richodemus.autoplaylist.spotify

import awaitStringResponse
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.result.Result
import com.richodemus.autoplaylist.dto.AlbumId
import com.richodemus.autoplaylist.dto.ArtistId
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.PlaylistName
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyPlaylistId
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.TrackUri
import org.slf4j.LoggerFactory
import java.util.Base64
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named
internal class SpotifyClient(
        @Named("spotifyUrl") private val apiUrl: String,
        @Named("spotifyAccountsUrl") private val accountsUrl: String,
        @Named("spotifyClientId") private val clientId: String,
        @Named("spotifyClientSecret") private val clientSecret: String,
        @Named("spotifyRedirectUrl") private val redirectUrl: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val mapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private val authString = "$clientId:$clientSecret"
            .let { "Basic " + Base64.getEncoder().encodeToString(it.toByteArray()) }

    internal suspend fun getTokens(code: String): Tokens {
        return Fuel.post("$accountsUrl/api/token",
                listOf(
                        "grant_type" to "authorization_code",
                        "code" to code,
                        "redirect_uri" to redirectUrl,
                        "client_id" to clientId,
                        "client_secret" to clientSecret
                ))
                .deserialize()
    }

    internal suspend fun refreshToken(refreshToken: RefreshToken): Tokens {
        return Fuel.post("$accountsUrl/api/token",
                listOf(
                        "grant_type" to "refresh_token",
                        "refresh_token" to refreshToken
                ))
                .header("Authorization" to authString)
                .deserialize()
    }

    internal suspend fun getUserId(accessToken: AccessToken): SpotifyUserId {
        return get("$apiUrl/v1/me", accessToken)
                .deserialize<User>()
                .id
    }

    internal suspend fun getPlaylists(accessToken: AccessToken): List<Playlist> {
        return get("$apiUrl/v1/me/playlists", accessToken)
                .deserialize<PlaylistsResponse>()
                .items
    }

    internal suspend fun findArtist(accessToken: AccessToken, name: ArtistName): List<Artist> {
        return Fuel.get("$apiUrl/v1/search",
                listOf(
                        "q" to name,
                        "type" to "artist"
                ))
                .header("Accept" to "application/json")
                .addHeaders(accessToken)
                .deserialize<FindArtistResponse>()
                .artists.items
    }

    internal suspend fun getArtist(accessToken: AccessToken, artistId: ArtistId): Artist? {
        return Fuel.get("$apiUrl/v1/artists/$artistId")
                .header("Accept" to "application/json")
                .addHeaders(accessToken)
                .deserialize() //todo proper exception handling and nullability
    }

    internal suspend fun getAlbums(accessToken: AccessToken, artistId: ArtistId): List<Album> {
        // todo don't hardcode market, figure out users market
        return get("$apiUrl/v1/artists/$artistId/albums?include_groups=album,single&market=SE", accessToken)
                .deserialize<GetAlbumsResponse>()
                .items
    }

    internal suspend fun getTracks(accessToken: AccessToken, album: AlbumId): List<Track> {
        return get("$apiUrl/v1/albums/$album/tracks", accessToken)
                .deserialize<GetTracksFromAlbumResponse>()
                .items
    }

    internal suspend fun createPlaylist(
            accessToken: AccessToken,
            name: PlaylistName,
            description: String,
            public: Boolean
    ): Playlist {
        return post("$apiUrl/v1/me/playlists", accessToken)
                .body("""
                {
                    "name":"$name",
                    "description":"$description",
                    "public":$public
                }
            """.trimIndent())
                .deserialize()
    }

    internal suspend fun getTracks(
            accessToken: AccessToken,
            playlistId: SpotifyPlaylistId
    ): List<Track> {
        return get("$apiUrl/v1/playlists/$playlistId/tracks", accessToken)
                .deserialize<GetTracksFromPlaylistResponse>()
                .items.map { it.track }
    }

    internal suspend fun addTracks(
            accessToken: AccessToken,
            playlist: SpotifyPlaylistId,
            tracks: List<TrackUri>
    ): SnapshotId {
        val request = AddTracksToPlaylistRequest(tracks)
        val json = mapper.writeValueAsString(request)

        return put("$apiUrl/v1/playlists/$playlist/tracks", accessToken)
                .body(json)
                .deserialize<AddTracksToPlaylistRespose>()
                .snapshot_id
    }

    private fun get(path: String, accessToken: AccessToken) = Fuel.get(path).addHeaders(accessToken)

    private fun post(path: String, accessToken: AccessToken) = Fuel.post(path).addHeaders(accessToken)

    private fun put(path: String, accessToken: AccessToken) = Fuel.put(path).addHeaders(accessToken)

    private fun Request.addHeaders(accessToken: AccessToken) = this.header(mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer $accessToken"
    ))

    private suspend inline fun <reified T : Any> Request.deserialize(): T {
        val (_, _, result) = this.awaitStringResponse()
        when (result) {
            is Result.Failure -> {
                val ex = result.getException()
                if (result.error.response.statusCode == 429) {
                    logger.warn("Rate limit exceeded")
                    val retryAfter = result.error.response.headers["Retry-After"]?.get(0)
                    if (retryAfter == null) {
                        logger.error("Missing retry-after header", ex)
                        throw ex
                    }
                    throw RateLimitExceededException(retryAfter.toLong())
                }
                logger.error("Call failed: ${result.error.response}", ex)
                throw ex
            }
            is Result.Success -> {
                val data = result.get()
                val playListsResponse: T
                try {
                    playListsResponse = mapper.readValue(data)
                    return playListsResponse
                } catch (e: Exception) {
                    logger.info("Unable to deserialize {}", data, e)
                    throw kotlin.RuntimeException("Unable to deserialize $data: ${e.message}")
                }
            }
        }
    }
}
