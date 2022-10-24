package com.richodemus.reader

import com.richodemus.reader.backend.LabelManager
import com.richodemus.reader.dto.Feed
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.FeedUrl
import com.richodemus.reader.dto.FeedWithoutItems
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.ItemOperation
import com.richodemus.reader.dto.ItemOperation.Operation.MARK_OLDER_ITEMS_AS_READ
import com.richodemus.reader.dto.ItemOperation.Operation.MARK_READ
import com.richodemus.reader.dto.ItemOperation.Operation.MARK_UNREAD
import com.richodemus.reader.dto.Label
import com.richodemus.reader.dto.User
import io.micrometer.core.annotation.Timed
import isLoggedIn
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import username
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


    @Suppress("unused")
    @Timed("getAllFeedsAndLabels")
    @GetMapping("/v1/feeds")
    internal fun getAllFeedsAndLabels(session: HttpSession): ResponseEntity<User> {
        val username = session.username
        if (!session.isLoggedIn() || username == null) {
            logger.warn("feeds called when not logged in: {}", username)
            return ResponseEntity(FORBIDDEN)
        }
        val feeds: List<FeedWithoutItems> = backendPort.getAllFeedsWithoutItems(username)
        val labels: List<Label> = labelManager.getLabels(username)
        return ResponseEntity.ok(User(feeds, labels))
    }

    @Suppress("unused")
    @GetMapping("v1/feeds/{id}")
    internal fun getFeed(session: HttpSession, @PathVariable("id") feedId: FeedId): ResponseEntity<Feed> {
        val username = session.username
        if (!session.isLoggedIn() || username == null) {
            logger.warn("feed called when not logged in: {}", username)
            return ResponseEntity(FORBIDDEN)
        }
        return backendPort.getFeed(username, feedId)
                ?.let { ResponseEntity.ok(it) }
                ?: ResponseEntity(BAD_REQUEST)
    }

    @Suppress("unused")
    @PostMapping("v1/feeds/{feedId}/items/{itemId}")
    internal fun performFeedOperation(
            session: HttpSession,
            @PathVariable("feedId") feedId: FeedId,
            @PathVariable("itemId") itemId: ItemId,
            @RequestBody operation: ItemOperation
    ): ResponseEntity<String> {
        val username = session.username
        if (!session.isLoggedIn() || username == null) {
            logger.warn("feed operation called when not logged in: {}", username)
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

    @Suppress("unused")
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
