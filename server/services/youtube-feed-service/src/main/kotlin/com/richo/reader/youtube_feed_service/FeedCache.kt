package com.richo.reader.youtube_feed_service

import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.FeedName
import org.slf4j.LoggerFactory
import java.util.ArrayList
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FeedCache
@Inject constructor(private val fileSystemPersistence: JsonFileSystemPersistence) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val cache = mutableMapOf<FeedId, Feed>()

    fun getAllFeedIds() = cache.keys

    operator fun get(feedId: FeedId): Optional<Feed> {
        logger.debug("Fetching channel {}", feedId)
        return Optional.ofNullable(cache.computeIfAbsent(feedId) {
            fileSystemPersistence.getChannel(feedId).orElse(null)
        })
    }

    fun update(feed: Feed) {
        logger.debug("Updating feed: {}, {} items", feed.id, feed.items.size)
        cache.put(feed.id, feed)
        fileSystemPersistence.updateChannel(feed)
    }

    fun add(feedId: FeedId) {
        logger.info("Adding feed {}", feedId)
        cache.putIfAbsent(feedId, Feed(feedId, FeedName("UNKNOWN_FEED"), ArrayList(), 0L))
    }
}
