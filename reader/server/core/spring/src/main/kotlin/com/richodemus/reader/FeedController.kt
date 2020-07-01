package com.richodemus.reader

import com.richodemus.reader.backend.LabelManager
import com.richodemus.reader.dto.*
import com.richodemus.reader.dto.ItemOperation.Operation.*
import io.micrometer.core.annotation.Timed
import isLoggedIn
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import userId
import username
import java.util.*
import java.util.function.Supplier
import javax.servlet.http.HttpSession

@CrossOrigin(
        origins = ["http://localhost:8080", "https://reader.richodemus.com"],
        maxAge = 3600,
        allowCredentials = "true"
)
@RestController
internal class FeedController(
        val backendPort: BackendPort,
        val labelManager: LabelManager
) {
    private val logger = LoggerFactory.getLogger(javaClass)


    @Timed("getAllFeedsAndLabels")
    @GetMapping("/v1/feeds")
    internal fun getAllFeedsAndLabels(session: HttpSession): ResponseEntity<User> {
        val username = session.username
        if (!session.isLoggedIn() || username == null) {
            return ResponseEntity(FORBIDDEN)
        }
        val feeds: List<FeedWithoutItems> = backendPort.getAllFeedsWithoutItems(username)
        val labels: List<Label> = labelManager.getLabels(username)
        return ResponseEntity.ok(User(feeds, labels))
    }

    @GetMapping("v1/feeds/{id}")
    internal fun getFeed(session: HttpSession, @PathVariable("id") feedId: FeedId): ResponseEntity<Feed> {
        val username = session.username
        if (!session.isLoggedIn() || username == null) {
            return ResponseEntity(FORBIDDEN)
        }
        return  backendPort.getFeed(username, feedId)
                ?.let { ResponseEntity.ok(it) }
                ?: ResponseEntity(BAD_REQUEST)
    }

    @PostMapping("v1/feeds/{feedId}/items/{itemId}")
    internal fun performFeedOperation(
            session: HttpSession,
            @PathVariable("feedId") feedId: FeedId,
            @PathVariable("itemId") itemId: ItemId,
            @RequestBody operation: ItemOperation
    ): ResponseEntity<String> {
        val username = session.username
        if (!session.isLoggedIn() || username == null) {
            return ResponseEntity(FORBIDDEN)
        }

        logger.info("Received item operation {} for feed {}, item {}", operation, feedId, itemId)
        when (operation.action) {
            MARK_READ -> backendPort.markAsRead(username, feedId, itemId)
            MARK_UNREAD -> backendPort.markAsUnread(username, feedId, itemId)
            MARK_OLDER_ITEMS_AS_READ -> backendPort.markOlderItemsAsRead(username, feedId, itemId)
        }

        return ResponseEntity.ok("\"OK\"")
    }

    @PostMapping("/v1/feeds")
    internal fun addFeed(
            session: HttpSession,
            @RequestBody feedUrl: FeedUrl
    ): ResponseEntity<String> {
        val username = session.username
        if (!session.isLoggedIn() || username == null) {
            return ResponseEntity(FORBIDDEN)
        }

        backendPort.addFeed(username, feedUrl)
        return ResponseEntity.ok("\"OK\"")
    }
}
