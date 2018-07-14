package com.richodemus.autoplaylist

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.result.Result
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import io.github.vjames19.futures.jdk8.map
import org.slf4j.LoggerFactory
import java.net.URL
import java.net.URLEncoder
import java.util.Base64
import java.util.UUID
import java.util.concurrent.CompletableFuture

private val logger = LoggerFactory.getLogger("SpotifyClient")
private val s = "${clientId()}:${clientSecret()}"
private val authString = "Basic " + Base64.getEncoder().encodeToString(s.toByteArray())

internal fun buildClientUrl() = URL(
        "https://accounts.spotify.com/authorize?response_type=code" +
                "&client_id=${clientId().encode()}" +
                "&scope=${scope()}" +
                "&redirect_uri=${redirectUrl().encode()}" +
                "&state=${UUID.randomUUID().toString().encode()}" +
                "&show_dialog=false"
)

internal fun getToken(code: String): CompletableFuture<Tokens> {
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

internal fun getUserId(accessToken: AccessToken): CompletableFuture<SpotifyUserId> {
    return Fuel.get("https://api.spotify.com/v1/me")
            .header("Content-Type" to "application/json")
            .header("Authorization" to "Bearer $accessToken")
            .deserialize<User>()
            .map { it.id }
}

internal fun getPlaylists(accessToken: AccessToken): CompletableFuture<PlayListsResponse> {
    return Fuel.get("https://api.spotify.com/v1/me/playlists")
            .header("Content-Type" to "application/json")
            .header("Authorization" to "Bearer $accessToken")
            .deserialize()
}

private fun refreshToken(refreshToken: RefreshToken): CompletableFuture<Tokens> {
    return Fuel.post("https://accounts.spotify.com/api/token",
            listOf(
                    "grant_type" to "refresh_token",
                    "refresh_token" to refreshToken
            ))
            .header("Authorization" to authString)
            .deserialize()
}

internal fun getAccessToken(refreshToken: RefreshToken) = refreshToken(refreshToken)

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

private fun String.encode() = URLEncoder.encode(this, "utf-8")

internal data class Tokens(
        @JsonProperty("access_token") val accessToken: AccessToken,
        @JsonProperty("scope") val scope: String,
        @JsonProperty("token_type") val tokenType: String,
        @JsonProperty("expires_in") val expiresIn: Int,
        @JsonProperty("refresh_token") val refreshToken: RefreshToken?
)

internal data class AccessToken(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "AccessToken can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}

internal data class User(val id: SpotifyUserId)

internal data class PlayListsResponse(val items: List<PlayList>, val total: Int)
internal data class PlayList(val id: PlayListId, val name: PlaylistName)
internal data class PlaylistName(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "PlaylistName can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}

internal data class PlayListId(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "PlayListId can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}

internal data class FindArtistResponse(val artists: Artists)
internal data class Artists(val items: List<Artist>, val total: Int)
internal data class Artist(val id: ArtistId, val name: ArtistName)
internal data class ArtistName(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "ArtistName can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}

internal data class ArtistId(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "ArtistId can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}

internal data class GetAlbumsResponse(val items: List<Album>, val total: Int)
internal data class Album(val id: AlbumId, val name: String, val album_group: String)
internal data class AlbumId(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "AlbumId can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}

internal data class GetTracksResponse(val items: List<Track>, val total: Int)
internal data class Track(val id: TrackId, val name: TrackName, val uri: TrackUri)
internal data class TrackId(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "TrackId can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}

internal data class TrackName(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "TrackName can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}

internal data class TrackUri(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "TrackUri can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}

internal data class AddTracksToPlaylistRequest(val uris: List<TrackUri>)
internal data class AddTracksToPlaylistRespose(val snapshot_id: SnapshotId)
internal data class SnapshotId(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "SnapshotId can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}