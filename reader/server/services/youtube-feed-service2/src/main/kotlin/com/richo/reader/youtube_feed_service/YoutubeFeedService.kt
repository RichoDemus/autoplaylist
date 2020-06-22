package com.richo.reader.youtube_feed_service

import arrow.core.Either
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.MetricRegistry.name
import com.richodemus.reader.common.google_cloud_storage_adapter.EventStore
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.FeedName
import com.richodemus.reader.dto.PlaylistId
import com.richodemus.reader.events_v2.EventType.USER_SUBSCRIBED_TO_FEED
import com.richodemus.reader.events_v2.UserSubscribedToFeed
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YoutubeFeedService @Inject
internal constructor(
        private val channelCache: Cache<Channel>,
        private val videoCache: Cache<Videos>,
        private val youtubeRepository: YoutubeRepository,
        registry: MetricRegistry,
        eventStore: EventStore
) {
    private val getChannelTimer = registry
            .timer(name(YoutubeFeedService::class.java, "getChannel"))
    private val getVideosTimer = registry
            .timer(name(YoutubeFeedService::class.java, "getVideos"))

    init {
        eventStore.consume { event ->
            if (event.type() === USER_SUBSCRIBED_TO_FEED) {
                val feedId = (event as UserSubscribedToFeed).feedId
                registerChannel(feedId)
            }
        }
    }

    private fun registerChannel(feedId: FeedId) {
        val either = youtubeRepository.getChannel(feedId)
        val channel = when (either) {
            is Either.Left -> {
                Channel(
                        feedId,
                        FeedName("UNKNOWN_FEED"),
                        PlaylistId("NOT_YET_FETCHED"),
                        OffsetDateTime.MIN,
                        OffsetDateTime.MIN
                )

            }
            is Either.Right -> {
                either.b
            }
        }
        channelCache[feedId.value] = channel
        videoCache[channel.playlistId.value] = Videos.empty()
    }

    fun getChannel(feedId: FeedId) = getChannelTimer.time().use {
        channelCache[feedId.value]
    }

    fun getVideos(feedId: FeedId) = getVideosTimer.time().use {
        val channel = channelCache[feedId.value]?:return null
        videoCache[channel.playlistId.value]
    }

    // this is used to add channels by url
//    fun getFeedId(feedUrl: FeedUrl) = youtubeChannelDownloader.getFeedId(feedUrl)

    internal fun updateChannelsAndVideos() {
        val playlists = videoCache.keys()

        val updatedVideos = playlists.map { Pair(it, emptyList<Video>()) }
                .map { (id, videos) ->
                    val vids = youtubeRepository.getVideos(PlaylistId(id))
                    Pair(id, Videos(vids))
                }

        updatedVideos.forEach { (id, videos) -> videoCache[id] = videos }

    }
}
