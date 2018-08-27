package com.richodemus.autoplaylist

import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.user.UserService
import io.github.vjames19.futures.jdk8.Future
import io.github.vjames19.futures.jdk8.flatMap
import io.github.vjames19.futures.jdk8.map
import io.github.vjames19.futures.jdk8.recover
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
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
internal class UserController @Inject internal constructor(
        val service: Service,
        val userService: UserService) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Suppress("unused")
    @GetMapping("/v1/sessions")
    internal fun getSession(session: HttpSession): ResponseEntity<String> {
        val userId = session.getUserId()
        logger.info("Checking session ${session.id} for $userId")
        val accessToken = userId?.let { userService.getUser(it) }?.accessToken

        if (accessToken != null) {
            return ResponseEntity(HttpStatus.OK)
        }
        return ResponseEntity(HttpStatus.NOT_FOUND)
    }

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
                .map { ResponseEntity(CreateSessionResponse("OK"), HttpStatus.OK) }
                .recover { exception ->
                    logger.error("login failed: {}", exception.message, exception)
                    ResponseEntity(
                            CreateSessionResponse("No refreshtoken"),
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                }
    }

    @Suppress("unused")
    @DeleteMapping("/v1/sessions")
    internal fun logout(session: HttpSession) {
        logger.info("logout")
        session.invalidate()
    }

    internal data class CreateSessionRequest(val code: String)

    internal data class CreateSessionResponse(val msg: String)

    internal data class GetUserIdResponse(val userId: SpotifyUserId)

    @Suppress("unused")
    @GetMapping("/v1/users/me")
    internal fun getUser(session: HttpSession): CompletableFuture<ResponseEntity<GetUserIdResponse>> {
        val userId = session.getUserId()
        if (userId == null) {
            logger.warn("No user for session {}", session.id)
            return Future { ResponseEntity<GetUserIdResponse>(HttpStatus.FORBIDDEN) }
        }

        return Future { userId }
                .flatMap { service.getSpotifyUserId(it!!) }
                .map { ResponseEntity(GetUserIdResponse(it), HttpStatus.OK) }
                .recover { exception ->
                    logger.error("getSpotifyUserId failed: {}", exception.message, exception)
                    ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }
}
