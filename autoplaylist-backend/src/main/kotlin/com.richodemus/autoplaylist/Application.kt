package com.richodemus.autoplaylist

import io.github.vjames19.futures.jdk8.flatMap
import io.github.vjames19.futures.jdk8.map
import io.github.vjames19.futures.jdk8.recover
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.CompletableFuture
import javax.servlet.http.HttpSession

@CrossOrigin(
        origins = ["http://localhost:3000", "https://autoplaylists.richodemus.com"],
        maxAge = 3600,
        allowCredentials = "true"
)
@RestController
@SpringBootApplication
open class Application {
    private val logger = LoggerFactory.getLogger(Application::class.java)

    @PostMapping("/sessions")
    internal fun createSession(session: HttpSession, @RequestBody request: CreateSessionRequest): CreateSessionResponse {
        logger.info("req: $request")
        val tokenFuture = getToken(request.code)
                .map {
                    logger.info("Tokens: {}", it)
                    it.accessToken
                }

        session.setAttribute("accessToken", tokenFuture.join())
        return CreateSessionResponse("OK")
    }

    internal data class CreateSessionResponse(val msg: String)

    @GetMapping("/users/me")
    internal fun getUser(session: HttpSession): CompletableFuture<UserId> {
        val accessToken = session.getAttribute("accessToken") as AccessToken
        logger.info("Session {} with token {}", session.id, accessToken)
        return getUserIdMemoized(accessToken)
    }

    @GetMapping("/playlists")
    internal fun getPlaylists(session: HttpSession): CompletableFuture<List<PlayList>> {
        val accessToken = session.getAttribute("accessToken") as AccessToken
        return getPlaylists(accessToken).map { it.items }
    }

    @PostMapping("/playlists")
    internal fun createPlayList(
            session: HttpSession,
            @RequestBody request: CreatePlaylistRequest
    ): CompletableFuture<CreatePlaylistResponse> {
        logger.info("Asked to create playlist {} with tracks by {}", request.name, request.artist)
        val accessToken = session.getAttribute("accessToken") as AccessToken
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

    }

    private val getUserIdMemoized = { accessToken: AccessToken -> getUserId(accessToken)}.memoize()
}

data class CreateSessionRequest(val code: String)

// todo move sessionId to auth header
internal data class CreatePlaylistRequest(val name: PlaylistName, val artist: ArtistName)

internal data class CreatePlaylistResponse(val successful: Boolean, val tracks: List<TrackName> = emptyList())

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
