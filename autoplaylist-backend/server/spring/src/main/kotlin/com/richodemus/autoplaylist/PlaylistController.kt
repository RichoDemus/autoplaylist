package com.richodemus.autoplaylist

import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.Track
import com.richodemus.autoplaylist.playlist.PlaylistWithAlbums
import com.richodemus.autoplaylist.spotify.Playlist
import com.richodemus.autoplaylist.spotify.PlaylistId
import com.richodemus.autoplaylist.spotify.PlaylistName
import io.github.vjames19.futures.jdk8.Future
import io.github.vjames19.futures.jdk8.flatMap
import io.github.vjames19.futures.jdk8.map
import io.github.vjames19.futures.jdk8.recover
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
internal class PlaylistController @Inject internal constructor(val service: Service) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Suppress("unused")
    @GetMapping("/v1/playlists")
    internal fun getPlaylists(session: HttpSession): CompletableFuture<ResponseEntity<List<Playlist>>> {
        val userId = session.getUserId() ?: return Future { ResponseEntity<List<Playlist>>(HttpStatus.FORBIDDEN) }
        logger.info("Get playlists for user {}", userId)
        return Future { userId }
                .flatMap { service.getPlaylists(it) }
                .map { ResponseEntity(it, HttpStatus.OK) }
                .recover { exception ->
                    logger.error("getPlaylists failed: {}", exception.message, exception)
                    ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }

    @Suppress("unused")
    @GetMapping("/v1/playlists/{id}/tracks")
    internal fun getTracks(
            session: HttpSession,
            @PathVariable("id") playlistId: String
    ): CompletableFuture<ResponseEntity<List<Track>>> {
        val userId = session.getUserId() ?: return Future { ResponseEntity<List<Track>>(HttpStatus.FORBIDDEN) }
        logger.info("Get tracks for playlist {}", playlistId)
        return Future { PlaylistId(playlistId) }
                .flatMap { service.getPlaylist(userId, it) }
                .map { ResponseEntity(it, HttpStatus.OK) }
                .recover { exception ->
                    logger.error("getPlaylists failed: {}", exception.message, exception)
                    ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }

    @Suppress("unused")
    @PostMapping("/v1/playlists")
    internal fun createPlaylist(session: HttpSession, @RequestBody request: CreatePlaylistRequest
    ): CompletableFuture<ResponseEntity<CreatePlaylistResponse>> {
        val userId = session.getUserId()
                ?: return Future { ResponseEntity<CreatePlaylistResponse>(HttpStatus.FORBIDDEN) }
        logger.info("Create playlist {}", request)
        return service.createPlaylist(userId, request.name, request.artist, request.exclusions.filterNot { it.isBlank() })
                .map { ResponseEntity.ok(CreatePlaylistResponse(playList = it)) }
                .recover {
                    ResponseEntity(
                            CreatePlaylistResponse(false, null),
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                }
    }

    internal data class CreatePlaylistRequest(
            val name: PlaylistName,
            val artist: ArtistName,
            val exclusions: List<String>
    )

    internal data class CreatePlaylistResponse(val successful: Boolean = true, val playList: PlaylistWithAlbums?)
}
