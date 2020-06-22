package com.richo.reader.youtube_feed_service

import arrow.core.Either
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.MetricRegistry.name
import com.google.common.collect.Lists
import com.richodemus.reader.common.google_cloud_storage_adapter.EventStore
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.FeedName
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.PlaylistId
import com.richodemus.reader.events_v2.EventType.USER_SUBSCRIBED_TO_FEED
import com.richodemus.reader.events_v2.UserSubscribedToFeed
import java.time.Duration
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
        val channel = channelCache[feedId.value] ?: return null
        videoCache[channel.playlistId.value]
    }

    // this is used to add channels by url
//    fun getFeedId(feedUrl: FeedUrl) = youtubeChannelDownloader.getFeedId(feedUrl)

    internal fun updateChannelsAndVideos() {
        val playlists = videoCache.keys()

        val updatedVideos = playlists.map { Pair(PlaylistId(it), emptyList<Video>()) }
                .map { (id, videos) ->
                    val lastUploaded = videos.sortedBy { it.uploadDate }.map { it.id }.firstOrNull()
                    val vids = youtubeRepository.getVideos(id, lastUploaded)
                    Pair(id, Videos(vids))
                }

        val allVideos = updatedVideos.flatMap { it.second.videos }.map { it.id }
        val partitioned: List<List<ItemId>> = Lists.partition(allVideos, 50)

        val withStatistics: Map<ItemId, Pair<Duration, Long>> = partitioned
                .map { youtubeRepository.getStatistics(it) }
                .fold(emptyMap()) { left, right -> left.plus(right) }


        val videosWithStatistics = updateVideosWithStatistics(updatedVideos, withStatistics)

        videosWithStatistics.forEach { (id, videos) -> videoCache[id.value] = videos }

    }

    private fun updateVideosWithStatistics(
            videos: List<Pair<PlaylistId, Videos>>, statistics: Map<ItemId, Pair<Duration, Long>>
    ): Map<PlaylistId, Videos> {
        return videos.map { (id, videos) -> Pair(id, videos.videos) }
                .map { (id, videos) ->
                    val updatedVideos = videos.map { video ->
                        statistics[video.id]?.let { stats ->
                            video.copy(duration = stats.first, views = stats.second)
                        }?:video
                    }
                    Pair(id, Videos(updatedVideos))
                }
                .toMap()
    }
}
