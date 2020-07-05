package com.richodemus.reader.youtube_feed_service

import arrow.core.Either
import arrow.core.orNull
import com.google.common.collect.Lists
import com.richodemus.reader.common.google_cloud_storage_adapter.EventStore
import com.richodemus.reader.dto.*
import com.richodemus.reader.events_v2.EventType.USER_SUBSCRIBED_TO_FEED
import com.richodemus.reader.events_v2.UserSubscribedToFeed
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.time.Duration
import java.time.OffsetDateTime

@Service
class YoutubeFeedService internal constructor(
        @Qualifier("channelCache") private val channelCache: Cache<FeedId, Channel>,
        @Qualifier("videoCache") private val videoCache: Cache<PlaylistId, Videos>,
        private val youtubeRepository: YoutubeRepository,
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
    val logger: Logger = LoggerFactory.getLogger(javaClass)

    private fun registerChannel(feedId: FeedId) {
        val channel = channelCache[feedId]

        if (channel != null) {
            return
        }

        val either = youtubeRepository.getChannel(feedId)
        val retrievedchannel = when (either) {
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
        channelCache[feedId] = retrievedchannel
    }

    fun getChannel(feedId: FeedId) = channelCache[feedId]

    fun getVideos(feedId: FeedId) : List<Video>{
        val channel = channelCache[feedId] ?: return emptyList()
        return videoCache[channel.playlistId]?.videos?: emptyList()
    }

    fun getFeedId(feedUrl: FeedUrl): FeedId? {
        val path = feedUrl.value.path;
        val id:String = if (path.startsWith("/user") || path.startsWith("/channel")) {
             path.split("/")[2];
        } else {
            throw IllegalArgumentException("Unsupported format: " + feedUrl.value)
        }
        if (path.startsWith("/channel/")) {
            return FeedId(id)
        }
        return youtubeRepository.getChannel(ChannelName(id)).orNull()
    }

    internal fun updateChannelsAndVideos() {
        logger.info("Syncing videos with youtube...")
        val playlists = channelCache.values().map { it.playlistId }

        val updatedVideos = playlists.map { Pair(it, emptyList<Video>()) }
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

        videosWithStatistics.forEach { (id, videos) -> videoCache[id] = videos }
        logger.info("Done downloading videos and statistics")

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
