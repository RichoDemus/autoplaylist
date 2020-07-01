package com.richodemus.reader

import com.richodemus.reader.dto.Password
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import com.richodemus.reader.user_service.UserService
import org.apache.http.HttpStatus
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import userId
import username
import javax.servlet.http.HttpSession

/**
 * curl -X POST -H "Content-Type: application/json" -d '{\"username\":\"Richo\", \"password\":\"my-pass\"}' --cookie curl-cookies --cookie-jar curl-cookies http://localhost:8080/v1/sessions
 * curl -X GET -H "Content-Type: application/json" --cookie curl-cookies --cookie-jar curl-cookies http://localhost:8080/v1/sessions
 * curl -X DELETE -H "Content-Type: application/json" --cookie curl-cookies --cookie-jar curl-cookies http://localhost:8080/v1/sessions
 */
@CrossOrigin(
        origins = ["http://localhost:8080", "https://reader.richodemus.com"],
        maxAge = 3600,
        allowCredentials = "true"
)
@RestController
internal class SessionController(
        val userService: UserService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Suppress("unused")
    @GetMapping("/v1/sessions")
    internal fun validateSession(session: HttpSession): ResponseEntity<String> {
        val user = session.userId ?: return ResponseEntity(UNAUTHORIZED)
        logger.debug("get session {}", user)
        return ResponseEntity.ok("OK: $user")
    }

    internal data class LoginRequest(val username: Username, val password: Password)

    @Suppress("unused")
    @PostMapping("/v1/sessions")
    internal fun login(session: HttpSession, @RequestBody request: LoginRequest) =
            if (userService.passwordValid(request.username, request.password)) {
                val id = userService.find(request.username)!!.id
                session.userId = id
                session.username = request.username
                ResponseEntity.ok("OK")
            } else {
                ResponseEntity(UNAUTHORIZED)
            }

    // todo implement logout in front end
    @Suppress("unused")
    @DeleteMapping("/v1/sessions")
    internal fun logout(session: HttpSession) {
        logger.debug("logout")
        session.invalidate()
    }
}
