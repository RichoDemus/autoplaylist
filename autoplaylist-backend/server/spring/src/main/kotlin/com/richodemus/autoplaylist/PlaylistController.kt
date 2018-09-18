package com.richodemus.autoplaylist

import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.Track
import com.richodemus.autoplaylist.playlist.PlaylistWithAlbums
import com.richodemus.autoplaylist.spotify.Playlist
import com.richodemus.autoplaylist.spotify.PlaylistId
import com.richodemus.autoplaylist.spotify.PlaylistName
import kotlinx.coroutines.experimental.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
internal class PlaylistController(val service: Service) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Suppress("unused")
    @GetMapping("/v1/playlists")
    internal fun getPlaylists(session: HttpSession): ResponseEntity<List<Playlist>> {
        val userId = session.getUserId()
                ?: return ResponseEntity(HttpStatus.FORBIDDEN)
        logger.info("Get playlists for user {}", userId)
        return runBlocking {
            ResponseEntity.ok(service.getPlaylists(userId))
        }
    }

    @Suppress("unused")
    @GetMapping("/v1/playlists/{id}/tracks")
    internal fun getTracks(
            session: HttpSession,
            @PathVariable("id") playlistId: String // todo use dto instead of string
    ): ResponseEntity<List<Track>> {
        val userId = session.getUserId()
                ?: return ResponseEntity(HttpStatus.FORBIDDEN)
        logger.info("Get tracks for playlist {}", playlistId)

        return runBlocking {
            try {
                ResponseEntity.ok(service.getPlaylist(userId, PlaylistId(playlistId)))
            } catch (e: Exception) {
                logger.error("getPlaylists failed: {}", e.message, e)
                ResponseEntity<List<Track>>(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    @Suppress("unused")
    @PostMapping("/v1/playlists")
    internal fun createPlaylist(session: HttpSession, @RequestBody request: CreatePlaylistRequest
    ): ResponseEntity<CreatePlaylistResponse> {
        val userId = session.getUserId()
                ?: return ResponseEntity(HttpStatus.FORBIDDEN)
        logger.info("Create playlist {}", request)

        return runBlocking {
            try {
                service.createPlaylist(userId, request.name, request.artist, request.exclusions.filterNot { it.isBlank() })
                        .let { ResponseEntity.ok(CreatePlaylistResponse(playList = it)) }
            } catch (e: Exception) {
                logger.error("Failed to create playlist", e)
                ResponseEntity(
                        CreatePlaylistResponse(false, null),
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
            }
        }
    }

    internal data class CreatePlaylistRequest(
            val name: PlaylistName,
            val artist: ArtistName,
            val exclusions: List<String>
    )

    internal data class CreatePlaylistResponse(val successful: Boolean = true, val playList: PlaylistWithAlbums?)
}
