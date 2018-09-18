package com.richodemus.autoplaylist

import com.richodemus.autoplaylist.dto.Artist
import com.richodemus.autoplaylist.dto.ArtistName
import kotlinx.coroutines.experimental.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpSession

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
}
