package com.richodemus.autoplaylist.spotify

import com.richodemus.autoplaylist.dto.AlbumId
import com.richodemus.autoplaylist.dto.AlbumName
import com.richodemus.autoplaylist.dto.ArtistId
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.TrackId
import com.richodemus.autoplaylist.dto.TrackName
import com.richodemus.autoplaylist.dto.TrackUri
import com.xebialabs.restito.builder.stub.StubHttp.whenHttp
import com.xebialabs.restito.semantics.Action.resourceContent
import com.xebialabs.restito.semantics.Action.status
import com.xebialabs.restito.semantics.Condition.get
import com.xebialabs.restito.semantics.Condition.parameter
import com.xebialabs.restito.semantics.Condition.post
import com.xebialabs.restito.semantics.Condition.withHeader
import com.xebialabs.restito.semantics.Condition.withPostBodyContaining
import com.xebialabs.restito.semantics.Condition.withPostBodyContainingJsonPath
import com.xebialabs.restito.server.StubServer
import org.assertj.core.api.Assertions.assertThat
import org.glassfish.grizzly.http.util.HttpStatus.OK_200
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Base64

internal class SpotifyClientTest {
    private val clientId = "client-id"
    private val clientSecret = "client-secret"
    private val redirectUrl = "redirect-url"
    private val code = "my-code"
    private val accessToken = AccessToken("access-token")
    private val refreshToken = RefreshToken("refresh-token")
    private val artistId = ArtistId("artist-id")
    private val albumId = AlbumId("album-id")
    private val userId = SpotifyUserId("user-id")
    private val playlistId = PlaylistId("playlist-id")
    private val playlistName = PlaylistName("playlist-name")
    private val description = "description"
    private val public = true
    private val snapshotId = SnapshotId("snapshot-id")

    private lateinit var server: StubServer
    private lateinit var target: SpotifyClient

    @Before
    fun setUp() {
        server = StubServer().run()
        target = SpotifyClient(
                "http://localhost:${server.port}",
                "http://localhost:${server.port}",
                clientId,
                clientSecret,
                redirectUrl
        )
    }

    @After
    fun tearDown() {
        server.stop()
    }

    @Test
    fun `Get tokens`() {
        whenHttp(server)
                .match(
                        post("/api/token"),
                        withHeader("content-type", "application/x-www-form-urlencoded"),
                        parameter("grant_type", "authorization_code"),
                        parameter("code", code),
                        parameter("redirect_uri", redirectUrl),
                        parameter("client_id", clientId),
                        parameter("client_secret", clientSecret)

                )
                .then(status(OK_200), resourceContent("spotify/getTokens.json"))

        val result = target.getTokens(code).join()

        assertThat(result).isEqualTo(Tokens(
                accessToken,
                "playlist-read-private",
                "Bearer",
                3600,
                refreshToken
        ))
    }

    @Test
    fun `Refresh token`() {
        whenHttp(server)
                .match(
                        post("/api/token"),
                        withHeader("authorization", "Basic ${authString()}"),
                        parameter("grant_type", "refresh_token"),
                        parameter("refresh_token", refreshToken.value)

                )
                .then(status(OK_200), resourceContent("spotify/refreshTokens.json"))

        val result = target.refreshToken(refreshToken).join()

        assertThat(result).isEqualTo(Tokens(
                AccessToken("new-access-token"),
                "playlist-read-private",
                "Bearer",
                3600,
                null
        ))
    }

    @Test
    fun `Get userId`() {
        whenHttp(server)
                .match(
                        get("/v1/me"),
                        withHeader("content-Type", "application/json"),
                        withHeader("authorization", "Bearer $accessToken")
                )
                .then(status(OK_200), resourceContent("spotify/getUserId.json"))

        val result = target.getUserId(accessToken).join()

        assertThat(result).isEqualTo(SpotifyUserId("wizzler"))
    }

    @Test
    fun `Get playlists`() {
        whenHttp(server)
                .match(
                        get("/v1/me/playlists"),
                        withHeader("content-Type", "application/json"),
                        withHeader("authorization", "Bearer $accessToken")
                )
                .then(status(OK_200), resourceContent("spotify/getPlaylists.json"))

        val result = target.getPlaylists(accessToken).join()

        assertThat(result).containsOnly(
                Playlist(PlaylistId("53Y8wT46QIMz5H4WQ8O22c"), PlaylistName("Wizzlers Big Playlist")),
                Playlist(PlaylistId("1AVZz0mBuGbCEoNRQdYQju"), PlaylistName("Another Playlist"))
        )
    }

    @Test
    fun `Find artist`() {
        whenHttp(server)
                .match(
                        get("/v1/search"),
                        parameter("q", "Civil War"),
                        parameter("type", "artist"),
                        withHeader("accept", "application/json"),
                        withHeader("content-Type", "application/json"),
                        withHeader("authorization", "Bearer $accessToken")

                )
                .then(status(OK_200), resourceContent("spotify/findArtist.json"))

        val result = target.findArtist(accessToken, ArtistName("Civil War")).join()

        assertThat(result).containsOnly(
                Artist(ArtistId("6lGzC0JJCotCU9QZ2Lgi8T"), ArtistName("Civil War")),
                Artist(ArtistId("2vCYRtpVXUjsoeD5fG0vat"), ArtistName("Civil War")),
                Artist(ArtistId("41rw6O2Slt6TRrVRKwia8Z"), ArtistName("Civil War Comrades")),
                Artist(ArtistId("1bjJlDNRMIFC8YY0JG8tEe"), ArtistName("The Civil War O.S.T.")),
                Artist(ArtistId("3RleSu7GotBHRQmaPFNQbD"), ArtistName("Civil War Rust")),
                Artist(ArtistId("5Kr5pcYZbWk0J0NQgPzTtB"), ArtistName("The Civil War Fiddlers")),
                Artist(ArtistId("6EkNXNEwjdt6eSaMLHNRxF"), ArtistName("Your Civil War")),
                Artist(ArtistId("2gPdhe0ZENU4uWNWWKvCIm"), ArtistName("Small Civil War")),
                Artist(ArtistId("6uWeWnwnBEKTYLTWCLuKFC"), ArtistName("Irish Volunteers Civil War Band")),
                Artist(ArtistId("2yVb5IT7HROeULYlafTR6G"), ArtistName("The Civil War Players")),
                Artist(ArtistId("1Bi7EjE7yhY7JoK579SBPx"), ArtistName("Choir Republican Civil War"))
        )
    }

    @Test
    fun `Get albums`() {
        whenHttp(server)
                .match(
                        get("/v1/artists/$artistId/albums"),
                        withHeader("content-Type", "application/json"),
                        withHeader("authorization", "Bearer $accessToken")
                )
                .then(status(OK_200), resourceContent("spotify/getAlbums.json"))

        val result = target.getAlbums(accessToken, artistId).join()

        assertThat(result).containsOnly(
                Album(
                        AlbumId("43977e0YlJeMXG77uCCSMX"),
                        AlbumName("Shut Up Lets Dance (Vol. II)"),
                        "appears_on"
                ),
                Album(
                        AlbumId("189ngoT3WxR5mZSYkAGOLF"),
                        AlbumName("Classic Club Monsters (25 Floor Killers)"),
                        "appears_on"
                )
        )
    }

    @Test
    fun `Get tracks in album`() {
        whenHttp(server)
                .match(
                        get("/v1/albums/$albumId/tracks"),
                        withHeader("content-Type", "application/json"),
                        withHeader("authorization", "Bearer $accessToken")
                )
                .then(status(OK_200), resourceContent("spotify/getTracksInAlbum.json"))

        val result = target.getTracks(accessToken, albumId).join()

        assertThat(result).containsOnly(
                Track(
                        TrackId("2TpxZ7JUBn3uw46aR7qd6V"),
                        TrackName("All I Want"),
                        TrackUri("spotify:track:2TpxZ7JUBn3uw46aR7qd6V")
                ),
                Track(
                        TrackId("4PjcfyZZVE10TFd9EKA72r"),
                        TrackName("Someday"),
                        TrackUri("spotify:track:4PjcfyZZVE10TFd9EKA72r")
                )
        )
    }

    @Test
    fun `Create playlist`() {
        whenHttp(server)
                .match(
                        post("/v1/me/playlists"),
                        withHeader("content-Type", "application/json"),
                        withHeader("authorization", "Bearer $accessToken"),
                        withPostBodyContainingJsonPath("name", playlistName.value),
                        withPostBodyContainingJsonPath("description", description),
                        withPostBodyContainingJsonPath("public", public)
                )
                .then(status(OK_200), resourceContent("spotify/createPlaylist.json"))

        val result = target.createPlaylist(accessToken, playlistName, description, public).join()

        assertThat(result).isEqualTo(Playlist(playlistId, playlistName))
    }

    @Test
    fun `Get tracks in playlist`() {
        whenHttp(server)
                .match(
                        get("/v1/playlists/$playlistId/tracks"),
                        withHeader("content-Type", "application/json"),
                        withHeader("authorization", "Bearer $accessToken")
                )
                .then(status(OK_200), resourceContent("spotify/getTracksInPlaylist.json"))

        val result = target.getTracks(accessToken, playlistId).join()

        assertThat(result).containsOnly(
                Track(
                        TrackId("7pk3EpFtmsOdj8iUhjmeCM"),
                        TrackName("Otra Vez (feat. J Balvin)"),
                        TrackUri("spotify:track:7pk3EpFtmsOdj8iUhjmeCM")
                )
        )
    }

    @Test
    fun `Add tracks to playlist`() {
        whenHttp(server)
                .match(
                        post("/v1/playlists/$playlistId/tracks"),
                        withHeader("content-Type", "application/json"),
                        withHeader("authorization", "Bearer $accessToken"),
                        withPostBodyContaining("""{"uris":["an-uri"]}""")
                )
                .then(status(OK_200), resourceContent("spotify/addTracksToPlaylist.json"))

        val result = target.addTracks(accessToken, playlistId, listOf(TrackUri("an-uri"))).join()

        assertThat(result).isEqualTo(snapshotId)
    }

    private fun authString() = "$clientId:$clientSecret".let { Base64.getEncoder().encodeToString(it.toByteArray()) }
}
