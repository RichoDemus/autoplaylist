package com.richodemus.autoplaylist

import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.user.UserService
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpSession

@CrossOrigin(
        origins = ["http://localhost:3000", "https://autoplaylists.richodemus.com"],
        maxAge = 3600,
        allowCredentials = "true"
)
@RestController
internal class UserController(
        val service: Service,
        val userService: UserService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Suppress("unused")
    @GetMapping("/v1/sessions")
    internal fun getSession(session: HttpSession): ResponseEntity<String> {
        val userId = session.getUserId()
        logger.info("Checking session ${session.id} for $userId")

        // todo check if we have a working access or refresh token
        val accessToken = ""
//        val accessToken = userId?.let { userService.getUser(it) }?.accessToken

        if (accessToken != null) {
            return ResponseEntity(HttpStatus.OK)
        }
        return ResponseEntity(HttpStatus.NOT_FOUND)
    }

    @Suppress("unused")
    @PostMapping("/v1/sessions")
    internal fun createSession(session: HttpSession, @RequestBody request: CreateSessionRequest
    ): ResponseEntity<CreateSessionResponse> {
        logger.info("req: $request")

        return runBlocking {
            try {
                val userId = service.login(request.code)
                session.setAttribute("userId", userId)
                ResponseEntity(CreateSessionResponse("OK"), HttpStatus.OK)
            } catch (e: Exception) {
                logger.error("login failed: {}", e.message, e)
                ResponseEntity(
                        CreateSessionResponse("No refreshtoken"),
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
            }
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
    internal fun getUser(session: HttpSession): ResponseEntity<GetUserIdResponse> {
        val userId = session.getUserId()
        if (userId == null) {
            logger.warn("No user for session {}", session.id)
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }

        return runBlocking {
            val spotifyUserId = service.getSpotifyUserId(userId)
            if(spotifyUserId == null) {
                logger.error("No spotify user id for user {}", userId)
                return@runBlocking ResponseEntity<GetUserIdResponse>(HttpStatus.NOT_FOUND)
            }
            return@runBlocking ResponseEntity.ok(GetUserIdResponse(spotifyUserId))
        }
    }
}
