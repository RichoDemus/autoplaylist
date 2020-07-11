package com.richodemus.reader

import com.richodemus.reader.youtube_feed_service.PeriodicDownloadOrchestrator
import isLoggedIn
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import javax.servlet.http.HttpSession

@CrossOrigin(
        origins = ["http://localhost:8080", "https://reader.richodemus.com"],
        maxAge = 3600,
        allowCredentials = "true"
)
@RestController
internal class AdminController(
        private val periodicDownloadOrchestrator: PeriodicDownloadOrchestrator
) {

    @Suppress("unused")
    @GetMapping("v1/admin/download")
    internal fun getStatus(): ResponseEntity<DownloadJobStatus> {
        return ResponseEntity.ok(DownloadJobStatus(
                periodicDownloadOrchestrator.lastRun(),
                periodicDownloadOrchestrator.isRunning,
                periodicDownloadOrchestrator.lastRunOutCome()
        ))
    }

    data class DownloadJobStatus(val lastRun: LocalDateTime, val running: Boolean, val lastRunOutCome: String)

    @Suppress("unused")
    @PostMapping("v1/admin/download")
    internal fun download(session: HttpSession): ResponseEntity<String> {
        if (!session.isLoggedIn()) {
            return ResponseEntity(FORBIDDEN)
        }
        periodicDownloadOrchestrator.downloadEverythingOnce()
        return ResponseEntity.ok("\"OK\"")
    }
}
