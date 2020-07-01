package com.richodemus.reader.youtube_feed_service

import com.richodemus.reader.youtube_feed_service.youtube.YoutubeChannelDownloader
import com.richodemus.reader.common.google_cloud_storage_adapter.EventStore
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.FeedUrl
import com.richodemus.reader.events_v2.EventType.USER_SUBSCRIBED_TO_FEED
import com.richodemus.reader.events_v2.UserSubscribedToFeed
import org.springframework.stereotype.Service

@Service
class YoutubeFeedService(
        private val cache: FeedCache,
        private val youtubeChannelDownloader: YoutubeChannelDownloader,
        eventStore: EventStore
) {

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

    fun getChannel(feedId: FeedId) = cache[feedId]

    fun getFeedId(feedUrl: FeedUrl) = youtubeChannelDownloader.getFeedId(feedUrl)
}
