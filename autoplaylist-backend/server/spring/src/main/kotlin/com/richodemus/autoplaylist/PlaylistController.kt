package com.richodemus.autoplaylist

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.richodemus.autoplaylist.dto.PlaylistId
import com.richodemus.autoplaylist.dto.PlaylistName
import com.richodemus.autoplaylist.dto.Rules
import com.richodemus.autoplaylist.dto.Track
import com.richodemus.autoplaylist.user.UserService
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.servlet.http.HttpSession

@CrossOrigin(
        origins = ["http://localhost:3000", "https://autoplaylist.richodemus.com"],
        maxAge = 3600,
        allowCredentials = "true"
)
@RestController
internal class PlaylistController(
        val service: Service,
        val userService: UserService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Suppress("unused")
    @GetMapping("/v1/playlists")
    internal fun getPlaylists(session: HttpSession): ResponseEntity<List<com.richodemus.autoplaylist.dto.Playlist>> {
        val userId = session.getUserId()
                ?: return ResponseEntity(FORBIDDEN)
        logger.info("Get playlists for user {}", userId)
        return runBlocking {
            ResponseEntity.ok(service.getPlaylists(userId).map { it.toDto() })
        }
    }

    @Suppress("unused")
    @GetMapping("/v1/playlists/{id}")
    internal fun getPlaylist(
            session: HttpSession,
            @PathVariable("id") playlistId: String
    ): ResponseEntity<com.richodemus.autoplaylist.dto.Playlist> {
        val userId = session.getUserId()
                ?: return ResponseEntity(FORBIDDEN)
        logger.info("Get playlists for user {}", userId)

        try {
            val user = userService.getUser(userId) ?: return ResponseEntity(NOT_FOUND)
            val playlist = user.getPlaylist(PlaylistId(UUID.fromString(playlistId)))
            val playlistDto = runBlocking { playlist.toDto() }
            return ResponseEntity.ok(playlistDto)

        } catch (e: Exception) {
            logger.error("getPlaylist failed: {}", e.message, e)
            return ResponseEntity(INTERNAL_SERVER_ERROR)
        }
    }

    @Suppress("unused")
    @GetMapping("/v1/playlists/{id}/tracks")
    internal fun getTracks(
            session: HttpSession,
            @PathVariable("id") playlistId: PlaylistId
    ): ResponseEntity<List<Track>> {
        val userId = session.getUserId()
                ?: return ResponseEntity(FORBIDDEN)
        logger.info("Get tracks for playlist {}", playlistId)

        return runBlocking {
            try {
//                ResponseEntity.ok(service.getPlaylist(userId, id))
                // todo need to get proper playlist Id
                TODO("Create endpoint to test rules and move this one there")
            } catch (e: Exception) {
                logger.error("getPlaylists failed: {}", e.message, e)
                ResponseEntity<List<Track>>(INTERNAL_SERVER_ERROR)
            }
        }
    }

    @Suppress("unused")
    @PostMapping("/v1/playlists")
    internal fun createPlaylist(
            session: HttpSession,
            @RequestBody request: CreatePlaylistRequest
    ) = runBlocking<ResponseEntity<CreatePlaylistResponse>> {
        val userId = session.getUserId()
                ?: return@runBlocking ResponseEntity(FORBIDDEN)
        logger.info("Create playlist {}", request)

        try {
            val playlist = service.createPlaylist(userId, request.name)
            val playlistDto = playlist.toDto()
            return@runBlocking ResponseEntity.ok(CreatePlaylistResponse(
                    true,
                    playlistDto
            ))
        } catch (e: Exception) {
            logger.error("Failed to create playlist", e)
            ResponseEntity(
                    CreatePlaylistResponse(false, null),
                    INTERNAL_SERVER_ERROR
            )
        }
    }

    @Suppress("unused")
    @PostMapping("/v1/playlists/{id}/rules")
    internal fun setRules(
            session: HttpSession,
            @PathVariable("id") playlistId: String,
            @RequestBody rawRules: String //todo let spring deserialize
    ) = runBlocking<ResponseEntity<com.richodemus.autoplaylist.dto.Playlist>> {
        val userId = session.getUserId()
                ?: return@runBlocking ResponseEntity(FORBIDDEN)
        logger.info("Raw rules: {}", rawRules)
        val rules = jacksonObjectMapper().readValue<Rules>(rawRules)
        logger.info("Set playlist rules: {}", rules)

        try {
            val user = userService.getUser(userId)
                    ?: return@runBlocking ResponseEntity<com.richodemus.autoplaylist.dto.Playlist>(NOT_FOUND)
            val playlist = user.getPlaylist(PlaylistId(UUID.fromString(playlistId)))
            playlist.overwriteRules(rules)

            val playlistDto = playlist.toDto()
            return@runBlocking ResponseEntity.ok(playlistDto)
        } catch (e: Exception) {
            logger.error("Failed to create playlist", e)
            ResponseEntity(INTERNAL_SERVER_ERROR)
        }
    }

    @Suppress("unused")
    @PostMapping("/v1/playlists/{id}/sync")
    internal fun setSync(
            session: HttpSession,
            @PathVariable("id") playlistId: String,
            @RequestBody enabled: Boolean
    ) = runBlocking<ResponseEntity<com.richodemus.autoplaylist.dto.Playlist>> {
        val userId = session.getUserId()
                ?: return@runBlocking ResponseEntity(FORBIDDEN)
        logger.info("Set sync to : {}", enabled)

        try {
            val user = userService.getUser(userId)
                    ?: return@runBlocking ResponseEntity<com.richodemus.autoplaylist.dto.Playlist>(NOT_FOUND)
            val playlist = user.getPlaylist(PlaylistId(UUID.fromString(playlistId)))
            if (enabled)
                playlist.startSyncinc()
            else
                playlist.stopSyncing()

            val playlistDto = playlist.toDto()
            return@runBlocking ResponseEntity.ok(playlistDto)
        } catch (e: Exception) {
            logger.error("Failed to create playlist", e)
            ResponseEntity(INTERNAL_SERVER_ERROR)
        }
    }

    @Suppress("unused")
    @PostMapping("/v1/playlists/{id}/syncOnce")
    internal fun sync(
            session: HttpSession,
            @PathVariable("id") playlistId: String
    ) = runBlocking<ResponseEntity<com.richodemus.autoplaylist.dto.Playlist>> {
        val userId = session.getUserId()
                ?: return@runBlocking ResponseEntity(FORBIDDEN)
        logger.info("Sync playlist {} once", playlistId)

        try {
            val user = userService.getUser(userId)
                    ?: return@runBlocking ResponseEntity<com.richodemus.autoplaylist.dto.Playlist>(NOT_FOUND)
            val playlist = user.getPlaylist(PlaylistId(UUID.fromString(playlistId)))
            playlist.sync()

            val playlistDto = playlist.toDto()
            return@runBlocking ResponseEntity.ok(playlistDto)
        } catch (e: Exception) {
            logger.error("Failed to create playlist", e)
            ResponseEntity(INTERNAL_SERVER_ERROR)
        }
    }

    internal data class CreatePlaylistRequest(val name: PlaylistName)

    internal data class CreatePlaylistResponse(val successful: Boolean = true, val playlist: com.richodemus.autoplaylist.dto.Playlist?)

    private suspend fun com.richodemus.autoplaylist.playlist.Playlist.toDto(): com.richodemus.autoplaylist.dto.Playlist {
        return com.richodemus.autoplaylist.dto.Playlist(
                this.id,
                this.spotifyPlaylistId,
                this.name,
                this.rules,
                this.tracksFromRules(),
                this.sync,
                this.lastSynced.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        )
    }
}
