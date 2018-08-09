package com.richodemus.autoplaylist

import com.richodemus.autoplaylist.dto.Artist
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.Track
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.playlist.PlaylistWithAlbums
import com.richodemus.autoplaylist.spotify.Playlist
import com.richodemus.autoplaylist.spotify.PlaylistId
import com.richodemus.autoplaylist.spotify.PlaylistName
import io.github.vjames19.futures.jdk8.Future
import io.github.vjames19.futures.jdk8.flatMap
import io.github.vjames19.futures.jdk8.map
import io.github.vjames19.futures.jdk8.recover
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.CompletableFuture
import javax.inject.Inject
import javax.servlet.http.HttpSession

@CrossOrigin(
        origins = ["http://localhost:3000", "https://autoplaylists.richodemus.com"],
        maxAge = 3600,
        allowCredentials = "true"
)
@RestController
@SpringBootApplication
@EnableScheduling
class Application @Inject internal constructor(private val service: Service) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Suppress("unused")
    @PostMapping("/v1/sessions")
    internal fun createSession(session: HttpSession, @RequestBody request: CreateSessionRequest
    ): CompletableFuture<ResponseEntity<CreateSessionResponse>> {
        logger.info("req: $request")

        return service.login(request.code)
                .map {
                    session.setAttribute("userId", it)
                    it
                }
                .map { ResponseEntity(CreateSessionResponse("OK"), OK) }
                .recover { exception ->
                    logger.error("login failed: {}", exception.message, exception)
                    ResponseEntity(
                            CreateSessionResponse("No refreshtoken"),
                            INTERNAL_SERVER_ERROR
                    )
                }
    }

    internal data class CreateSessionResponse(val msg: String)

    internal data class GetUserIdResponse(val userId: SpotifyUserId)

    @Suppress("unused")
    @GetMapping("/v1/users/me")
    internal fun getUser(session: HttpSession): CompletableFuture<ResponseEntity<GetUserIdResponse>> {
        val userId = session.getUserId()
        if (userId == null) {
            logger.warn("No user for session {}", session.id)
            return Future { ResponseEntity<GetUserIdResponse>(FORBIDDEN) }
        }

        return Future { userId }
                .flatMap { service.getSpotifyUserId(it!!) }
                .map { ResponseEntity(GetUserIdResponse(it), OK) }
                .recover { exception ->
                    logger.error("getSpotifyUserId failed: {}", exception.message, exception)
                    ResponseEntity(INTERNAL_SERVER_ERROR)
                }
    }

    @Suppress("unused")
    @GetMapping("/v1/playlists")
    internal fun getPlaylists(session: HttpSession): CompletableFuture<ResponseEntity<List<Playlist>>> {
        val userId = session.getUserId() ?: return Future { ResponseEntity<List<Playlist>>(FORBIDDEN) }
        logger.info("Get playlists for user {}", userId)
        return Future { userId }
                .flatMap { service.getPlaylists(it) }
                .map { ResponseEntity(it, OK) }
                .recover { exception ->
                    logger.error("getPlaylists failed: {}", exception.message, exception)
                    ResponseEntity(INTERNAL_SERVER_ERROR)
                }
    }

    @Suppress("unused")
    @GetMapping("/v1/playlists/{id}/tracks")
    internal fun getTracks(
            session: HttpSession,
            @PathVariable("id") playlistId: String
    ): CompletableFuture<ResponseEntity<List<Track>>> {
        val userId = session.getUserId() ?: return Future { ResponseEntity<List<Track>>(FORBIDDEN) }
        logger.info("Get tracks for playlist {}", playlistId)
        return Future { PlaylistId(playlistId) }
                .flatMap { service.getPlaylist(userId, it) }
                .map { ResponseEntity(it, OK) }
                .recover { exception ->
                    logger.error("getPlaylists failed: {}", exception.message, exception)
                    ResponseEntity(INTERNAL_SERVER_ERROR)
                }
    }

    @Suppress("unused")
    @PostMapping("/v1/playlists")
    internal fun createPlaylist(session: HttpSession, @RequestBody request: CreatePlaylistRequest
    ): CompletableFuture<ResponseEntity<CreatePlaylistResponse>> {
        val userId = session.getUserId() ?: return Future { ResponseEntity<CreatePlaylistResponse>(FORBIDDEN) }
        logger.info("Create playlist {}", request)
        return service.createPlaylist(userId, request.name, request.artist)
                .map { ResponseEntity.ok(CreatePlaylistResponse(playList = it)) }
                .recover {
                    ResponseEntity(
                            CreatePlaylistResponse(false, null),
                            INTERNAL_SERVER_ERROR
                    )
                }
    }

    @Suppress("unused")
    @GetMapping("/v1/artists")
    internal fun findArtists(
            session: HttpSession,
            @RequestParam("name") name: ArtistName
    ): CompletableFuture<ResponseEntity<List<Artist>>> {
        val userId = session.getUserId() ?: return Future { ResponseEntity<List<Artist>>(FORBIDDEN) }
        return Future { userId }
                .flatMap { service.findArtists(it, name) }
                .map { ResponseEntity.ok(it) }
                .recover {
                    ResponseEntity(
                            emptyList(),
                            INTERNAL_SERVER_ERROR
                    )
                }
    }

    private fun HttpSession.getUserId(): UserId? {
        return this.getAttribute("userId") as UserId?
    }

}

data class CreateSessionRequest(val code: String)

internal data class CreatePlaylistRequest(val name: PlaylistName, val artist: ArtistName)

internal data class CreatePlaylistResponse(val successful: Boolean = true, val playList: PlaylistWithAlbums?)

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
