package com.richodemus.reader

import com.google.common.base.Strings
import com.richodemus.reader.dto.*
import com.richodemus.reader.label_service.LabelService
import isLoggedIn
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import userId
import java.util.*
import javax.annotation.security.RolesAllowed
import javax.servlet.http.HttpSession

@CrossOrigin(
        origins = ["http://localhost:8080", "https://reader.richodemus.com"],
        maxAge = 3600,
        allowCredentials = "true"
)
@RestController
internal class LabelController(
private val labelService: LabelService
) {

    @PostMapping("v1/labels")
    internal fun createLabel(session: HttpSession, @RequestBody labelName:String): ResponseEntity<Label> {
        val userId = session.userId
        if (!session.isLoggedIn() || userId == null) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }

        val label = labelService.create(LabelName(labelName), userId)
        return ResponseEntity.ok(Label(label.id, label.name, label.feeds))
    }

    @PostMapping("v1/labels/{labelId}")
    internal fun addFeedToLabel(
            session: HttpSession,
            @PathVariable("labelId") labelIdStr: String,
            @RequestBody feedId: FeedId
    ) :ResponseEntity<String> {
        val labelId = LabelId(UUID.fromString(labelIdStr))
        val userId = session.userId
        if (!session.isLoggedIn() || userId == null) {
            return ResponseEntity(HttpStatus.FORBIDDEN)
        }

        // todo it's possible to add a feed to another users label
        labelService.addFeedToLabel(labelId, feedId)
        return ResponseEntity.ok("OK")
    }
}
