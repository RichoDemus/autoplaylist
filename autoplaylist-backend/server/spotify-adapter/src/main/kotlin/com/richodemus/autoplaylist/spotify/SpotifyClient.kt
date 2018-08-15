package com.richodemus.autoplaylist.spotify

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.result.Result
import com.richodemus.autoplaylist.dto.AlbumId
import com.richodemus.autoplaylist.dto.ArtistId
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.TrackUri
import io.github.vjames19.futures.jdk8.map
import org.slf4j.LoggerFactory
import java.util.Base64
import java.util.concurrent.CompletableFuture
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

    internal fun getTokens(code: String): CompletableFuture<Tokens> {
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

    internal fun refreshToken(refreshToken: RefreshToken): CompletableFuture<Tokens> {
        return Fuel.post("$accountsUrl/api/token",
                listOf(
                        "grant_type" to "refresh_token",
                        "refresh_token" to refreshToken
                ))
                .header("Authorization" to authString)
                .deserialize()
    }

    internal fun getUserId(accessToken: AccessToken): CompletableFuture<SpotifyUserId> {
        return get("$apiUrl/v1/me", accessToken)
                .deserialize<User>()
                .map { it.id }
    }

    internal fun getPlaylists(accessToken: AccessToken): CompletableFuture<List<Playlist>> {
        return get("$apiUrl/v1/me/playlists", accessToken)
                .deserialize<PlaylistsResponse>()
                .map { it.items }
    }

    internal fun findArtist(accessToken: AccessToken, name: ArtistName): CompletableFuture<List<Artist>> {
        return Fuel.get("$apiUrl/v1/search",
                listOf(
                        "q" to name,
                        "type" to "artist"
                ))
                .header("Accept" to "application/json")
                .addHeaders(accessToken)
                .deserialize<FindArtistResponse>()
                .map { it.artists.items }
    }

    internal fun getAlbums(accessToken: AccessToken, artistId: ArtistId): CompletableFuture<List<Album>> {
        return get("$apiUrl/v1/artists/$artistId/albums", accessToken)
                .deserialize<GetAlbumsResponse>()
                .map { it.items }
    }

    internal fun getTracks(accessToken: AccessToken, album: AlbumId): CompletableFuture<List<Track>> {
        return get("$apiUrl/v1/albums/$album/tracks", accessToken)
                .deserialize<GetTracksFromAlbumResponse>()
                .map { it.items }
    }

    internal fun createPlaylist(
            accessToken: AccessToken,
            name: PlaylistName,
            description: String,
            public: Boolean
    ): CompletableFuture<Playlist> {
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

    internal fun getTracks(
            accessToken: AccessToken,
            playlistId: PlaylistId
    ): CompletableFuture<List<Track>> {
        return get("$apiUrl/v1/playlists/$playlistId/tracks", accessToken)
                .deserialize<GetTracksFromPlaylistResponse>()
                .map { resp -> resp.items.map { it.track } }
    }

    internal fun addTracks(
            accessToken: AccessToken,
            playlist: PlaylistId,
            tracks: List<TrackUri>
    ): CompletableFuture<SnapshotId> {
        val request = AddTracksToPlaylistRequest(tracks)
        val json = mapper.writeValueAsString(request)

        return post("$apiUrl/v1/playlists/$playlist/tracks", accessToken)
                .body(json)
                .deserialize<AddTracksToPlaylistRespose>()
                .map { it.snapshot_id }
    }

    private fun get(path: String, accessToken: AccessToken) = Fuel.get(path).addHeaders(accessToken)

    private fun post(path: String, accessToken: AccessToken) = Fuel.post(path).addHeaders(accessToken)

    private fun Request.addHeaders(accessToken: AccessToken) = this.header(mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer $accessToken"
    ))

    private inline fun <reified T : Any> Request.deserialize(): CompletableFuture<T> {
        val future = CompletableFuture<T>()
        this.responseString { _, _, result ->
            when (result) {
                is Result.Failure -> {
                    val ex = result.getException()
                    if (result.error.response.statusCode == 429) {
                        logger.warn("Rate limit exceeded")
                        val retryAfter = result.error.response.headers["Retry-After"]?.get(0)
                        if (retryAfter == null) {
                            logger.error("Missing retry-after header", ex)
                            future.completeExceptionally(ex)
                            return@responseString
                        }
                        future.completeExceptionally(RateLimitExceededException(retryAfter.toLong()))
                        return@responseString
                    }
                    logger.error("Call failed: ${result.error.response}", ex)
                    future.completeExceptionally(ex)
                }
                is Result.Success -> {
                    val data = result.get()
                    val playListsResponse: T
                    try {
                        playListsResponse = mapper.readValue(data)
                        future.complete(playListsResponse)
                    } catch (e: Exception) {
                        logger.info("Unable to deserialize {}", data, e)
                        throw kotlin.RuntimeException("Unable to deserialize $data: ${e.message}")
                    }
                }
            }
        }
        return future
    }

}
