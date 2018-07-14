package com.richodemus.autoplaylist

import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.user.UserService
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
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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
open class Application @Inject internal constructor(private val userService: UserService) {
    private val logger = LoggerFactory.getLogger(Application::class.java)

    @PostMapping("/sessions")
    internal fun createSession(session: HttpSession, @RequestBody request: CreateSessionRequest
    ): ResponseEntity<CreateSessionResponse> {
        logger.info("req: $request")
        val tokenFuture = getToken(request.code)
                .map {
                    logger.info("Tokens: {}", it)
                    it
                }

        val userFuture = tokenFuture.flatMap { getUserIdMemoized(it.accessToken) }
                .flatMap { userService.findOrCreateUser(it) }

        val user = userFuture.join()
        val refreshToken = tokenFuture.join().refreshToken
                ?: return ResponseEntity(CreateSessionResponse("No refreshtoken"), INTERNAL_SERVER_ERROR)
        user.refreshToken = refreshToken
        session.setAttribute("userId", user.userId)
        logger.info("Created user: {}", user)
        return ResponseEntity(CreateSessionResponse("OK"), OK)
    }

    internal data class CreateSessionResponse(val msg: String)

    @GetMapping("/users/me")
    internal fun getUser(session: HttpSession): CompletableFuture<ResponseEntity<SpotifyUserId>> {
        val accessToken = session.getToken() ?: return Future { ResponseEntity<SpotifyUserId>(FORBIDDEN) }
        logger.info("Session {} with token {}", session.id, accessToken)
        return getUserIdMemoized(accessToken)
                .map { ResponseEntity(it, OK) }
    }

    @GetMapping("/playlists")
    internal fun getPlaylists(session: HttpSession): CompletableFuture<ResponseEntity<List<PlayList>>> {
        val accessToken = session.getToken()
                ?: return Future { ResponseEntity<List<PlayList>>(FORBIDDEN) }
        return getPlaylists(accessToken)
                .map { ResponseEntity(it.items, OK) }
    }

    @PostMapping("/playlists")
    internal fun createPlayList(
            session: HttpSession,
            @RequestBody request: CreatePlaylistRequest
    ): CompletableFuture<ResponseEntity<CreatePlaylistResponse>> {
        logger.info("Asked to create playlist {} with tracks by {}", request.name, request.artist)
        val accessToken = session.getToken()
                ?: return Future { ResponseEntity<CreatePlaylistResponse>(FORBIDDEN) }
        return getUserIdMemoized(accessToken)
                .flatMap { userId ->
                    createPlayListFromArtist(accessToken, userId, request.name, request.artist)
                }
                .map {
                    if (it.isNotEmpty())
                        CreatePlaylistResponse(true, it)
                    else
                        CreatePlaylistResponse(false)
                }
                .recover { e ->
                    logger.info("Failed to create playlist {}", request, e)
                    CreatePlaylistResponse(false)
                }
                .map { ResponseEntity(it, OK) }

    }

    private val getUserIdMemoized = { accessToken: AccessToken -> getUserId(accessToken) }.memoize()

    private fun HttpSession.getToken(): AccessToken? {
        val userId = this.getAttribute("userId") as UserId?
        return userId?.let { userService.getUser(it) }?.accessToken
    }

}

data class CreateSessionRequest(val code: String)

internal data class CreatePlaylistRequest(val name: PlaylistName, val artist: ArtistName)

internal data class CreatePlaylistResponse(val successful: Boolean, val tracks: List<TrackName> = emptyList())

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
