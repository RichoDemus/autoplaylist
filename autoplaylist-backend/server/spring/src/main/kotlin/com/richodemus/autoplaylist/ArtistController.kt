package com.richodemus.autoplaylist

import com.richodemus.autoplaylist.dto.Artist
import com.richodemus.autoplaylist.dto.ArtistId
import com.richodemus.autoplaylist.dto.ArtistName
import kotlinx.coroutines.experimental.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpSession

@CrossOrigin(
        origins = ["http://localhost:3000", "https://autoplaylists.richodemus.com"],
        maxAge = 3600,
        allowCredentials = "true"
)
@RestController
internal class ArtistController(private val service: Service) {
    @Suppress("unused")
    @GetMapping("/v1/artists")
    internal fun findArtists(
            session: HttpSession,
            @RequestParam("name") name: ArtistName
    ): ResponseEntity<List<Artist>> {
        val userId = session.getUserId()
                ?: return ResponseEntity(HttpStatus.FORBIDDEN)

        return runBlocking {
            try {
                ResponseEntity.ok(service.findArtists(userId, name))
            } catch (e: Exception) {
                ResponseEntity<List<Artist>>(
                        emptyList(),
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
            }
        }
    }

    @Suppress("unused")
    @GetMapping("/v1/artists/{id}")
    internal fun getArtist(
            session: HttpSession,
            @PathVariable("id") artistId: ArtistId
    ): ResponseEntity<Artist> {
        val userId = session.getUserId()
                ?: return ResponseEntity(HttpStatus.FORBIDDEN)

        return runBlocking {
            try {
                val artist = service.getArtist(userId, artistId)?:throw Exception("")
                ResponseEntity.ok(artist)
            } catch (e: Exception) {
                ResponseEntity<Artist>(
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
            }
        }
    }
}
