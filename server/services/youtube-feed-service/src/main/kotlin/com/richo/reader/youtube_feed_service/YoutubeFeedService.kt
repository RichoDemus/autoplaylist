package com.richo.reader.youtube_feed_service

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.MetricRegistry.name
import com.richo.reader.youtube_feed_service.youtube.YoutubeChannelDownloader
import com.richodemus.reader.common.kafka_adapter.EventStore
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.FeedUrl
import com.richodemus.reader.events_v2.EventType.USER_SUBSCRIBED_TO_FEED
import com.richodemus.reader.events_v2.UserSubscribedToFeed
import javax.inject.Inject

class YoutubeFeedService @Inject
internal constructor(private val cache: FeedCache,
            private val youtubeChannelDownloader: YoutubeChannelDownloader,
            registry: MetricRegistry,
            eventStore: EventStore) {
    private val getChannelTimer = registry
            .timer(name(YoutubeFeedService::class.java, "getChannel"))

    init {
        eventStore.consume { event ->
            if (event.type() === USER_SUBSCRIBED_TO_FEED) {
                val feedId = (event as UserSubscribedToFeed).feedId
                registerChannel(feedId)
            }
        }
    }

    private fun registerChannel(feedId: FeedId) {
        cache.add(feedId)
    }

    fun getChannel(feedId: FeedId) = getChannelTimer.time().use {
        cache[feedId]
    }

    fun getFeedId(feedUrl: FeedUrl) = youtubeChannelDownloader.getFeedId(feedUrl)
}
