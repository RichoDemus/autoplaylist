package com.richodemus.autoplaylist

import com.richodemus.autoplaylist.dto.Artist
import com.richodemus.autoplaylist.dto.ArtistName
import io.github.vjames19.futures.jdk8.Future
import io.github.vjames19.futures.jdk8.flatMap
import io.github.vjames19.futures.jdk8.map
import io.github.vjames19.futures.jdk8.recover
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.CompletableFuture
import javax.servlet.http.HttpSession

@RestController
internal class ArtistController(private val service: Service) {
    @Suppress("unused")
    @GetMapping("/v1/artists")
    internal fun findArtists(
            session: HttpSession,
            @RequestParam("name") name: ArtistName
    ): CompletableFuture<ResponseEntity<List<Artist>>> {
        val userId = session.getUserId() ?: return Future { ResponseEntity<List<Artist>>(HttpStatus.FORBIDDEN) }
        return Future { userId }
                .flatMap { service.findArtists(it, name) }
                .map { ResponseEntity.ok(it) }
                .recover {
                    ResponseEntity(
                            emptyList(),
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                }
    }
}
