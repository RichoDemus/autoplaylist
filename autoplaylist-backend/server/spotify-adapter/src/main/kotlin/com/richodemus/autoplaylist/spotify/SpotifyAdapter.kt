package com.richodemus.autoplaylist.spotify

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.result.Result
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import io.github.vjames19.futures.jdk8.map
import org.slf4j.LoggerFactory
import java.util.Base64
import java.util.concurrent.CompletableFuture
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named
internal class SpotifyAdapter : SpotifyPort {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val mapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    private val s = "${clientId()}:${clientSecret()}"
    private val authString = "Basic " + Base64.getEncoder().encodeToString(s.toByteArray())

    override fun getToken(code: String): CompletableFuture<Tokens> {
        return Fuel.post("https://accounts.spotify.com/api/token",
                listOf(
                        "grant_type" to "authorization_code",
                        "code" to code,
                        "redirect_uri" to redirectUrl(),
                        "client_id" to clientId(),
                        "client_secret" to clientSecret()
                ))
                .deserialize()
    }

    override fun getUserId(accessToken: AccessToken): CompletableFuture<SpotifyUserId> {
        return Fuel.get("https://api.spotify.com/v1/me")
                .header("Content-Type" to "application/json")
                .header("Authorization" to "Bearer $accessToken")
                .deserialize<User>()
                .map { it.id }
    }

    override fun getPlaylists(accessToken: AccessToken): CompletableFuture<List<PlayList>> {
        return Fuel.get("https://api.spotify.com/v1/me/playlists")
                .header("Content-Type" to "application/json")
                .header("Authorization" to "Bearer $accessToken")
                .deserialize<PlayListsResponse>()
                .map { it.items }
    }

    override fun refreshToken(refreshToken: RefreshToken): CompletableFuture<Tokens> {
        return Fuel.post("https://accounts.spotify.com/api/token",
                listOf(
                        "grant_type" to "refresh_token",
                        "refresh_token" to refreshToken
                ))
                .header("Authorization" to authString)
                .deserialize()
    }

    internal fun findArtist(accessToken: AccessToken, name: ArtistName): CompletableFuture<List<Artist>> {
        return Fuel.get("https://api.spotify.com/v1/search",
                listOf(
                        "q" to name,
                        "type" to "artist"
                ))
                .header("Content-Type" to "application/json")
                .header("Authorization" to "Bearer $accessToken")
                .deserialize<FindArtistResponse>()
                .map { it.artists.items }
    }

    internal fun getAlbums(accessToken: AccessToken, artist: ArtistId): CompletableFuture<List<Album>> {
        return Fuel.get("https://api.spotify.com/v1/artists/$artist/albums")
                .header("Content-Type" to "application/json")
                .header("Authorization" to "Bearer $accessToken")
                .deserialize<GetAlbumsResponse>()
                .map { it.items }
    }

    internal fun getTracks(accessToken: AccessToken, album: AlbumId): CompletableFuture<List<Track>> {
        return Fuel.get("https://api.spotify.com/v1/albums/$album/tracks")
                .header("Content-Type" to "application/json")
                .header("Authorization" to "Bearer $accessToken")
                .deserialize<GetTracksResponse>()
                .map { it.items }
    }

    internal fun createPlaylist(accessToken: AccessToken,
                                spotifyUserId: SpotifyUserId,
                                name: PlaylistName,
                                description: String,
                                public: Boolean): CompletableFuture<PlayList> {
        return Fuel.post("https://api.spotify.com/v1/users/$spotifyUserId/playlists")
                .header("Content-Type" to "application/json")
                .header("Authorization" to "Bearer $accessToken")
                .body("""
                {
                    "name":"$name",
                    "description":"$description",
                    "public":$public
                }
            """.trimIndent())
                .deserialize()
    }

    internal fun addTracks(accessToken: AccessToken,
                           user: SpotifyUserId,
                           playList: PlayListId,
                           tracks: List<TrackUri>): CompletableFuture<SnapshotId> {
        val request = AddTracksToPlaylistRequest(tracks)
        val json = mapper.writeValueAsString(request)


        return Fuel.post("https://api.spotify.com/v1/users/$user/playlists/$playList/tracks")
                .header("Content-Type" to "application/json")
                .header("Authorization" to "Bearer $accessToken")
                .body(json)
                .deserialize<AddTracksToPlaylistRespose>()
                .map { it.snapshot_id }
    }

    private inline fun <reified T : Any> Request.deserialize(): CompletableFuture<T> {
        val future = CompletableFuture<T>()
        this.responseString { _, _, result ->
            when (result) {
                is Result.Failure -> {
                    val ex = result.getException()
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