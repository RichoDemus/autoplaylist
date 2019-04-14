package com.richodemus.autoplaylist

import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.user.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpSession

@CrossOrigin(
        origins = ["http://localhost:3000", "https://autoplaylists.richodemus.com"],
        maxAge = 3600,
        allowCredentials = "true"
)
@RestController
class AdminController(val userService: UserService) {
    private val logger = LoggerFactory.getLogger(AdminController::class.java)

    @Suppress("unused")
    @GetMapping("/v1/admin/users")
    internal fun getUsers(session: HttpSession): ResponseEntity<List<WebUser>> {
        logger.info("Get users")
        val userId = session.getUserId() ?: return ResponseEntity(HttpStatus.FORBIDDEN)
        userService.getUser(userId)?.let {
//            if (it.spotifyUserId == SpotifyUserId("richodemus")) {
            if(false){ // todo fix amind thing
                return@let it
            }
            logger.warn("User {} tried to access admin stuff", userId)
            return@let null
        } ?: return ResponseEntity(HttpStatus.FORBIDDEN)

        return ResponseEntity.ok(
                userService.users.toList()
                        .map { it.second }
                        .map { WebUser(it.userId, SpotifyUserId("todo fix :D")) }
        )
    }

    private fun HttpSession.getUserId(): UserId? {
        return this.getAttribute("userId") as UserId?
    }

    data class WebUser(val userId: UserId, val spotifyUserId: SpotifyUserId)
}
