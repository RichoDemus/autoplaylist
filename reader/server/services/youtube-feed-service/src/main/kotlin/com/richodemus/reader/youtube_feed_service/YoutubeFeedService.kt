package com.richodemus.reader.youtube_feed_service

import arrow.core.Either
import com.google.common.collect.Lists
import com.richodemus.reader.common.google_cloud_storage_adapter.EventStore
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.FeedName
import com.richodemus.reader.dto.FeedUrl
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.PlaylistId
import com.richodemus.reader.events_v2.EventType.USER_SUBSCRIBED_TO_FEED
import com.richodemus.reader.events_v2.UserSubscribedToFeed
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Duration
import java.time.OffsetDateTime

@Service
class YoutubeFeedService internal constructor(
        @Qualifier("channelCache") private val channelCache: Cache<FeedId, Channel>,
        @Qualifier("videoCache") private val videoCache: Cache<PlaylistId, Videos>,
        private val youtubeRepository: YoutubeRepository,
        eventStore: EventStore,
        private val clock: Clock,
        private val prioritizer: StatisticsUpdatePrioritizer
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

    fun getVideos(feedId: FeedId): List<Video> {
        val channel = channelCache[feedId] ?: return emptyList()
        return videoCache[channel.playlistId]?.videos ?: emptyList()
    }

    fun getFeedId(feedUrl: FeedUrl): FeedId? {
        val path = feedUrl.value.path
        val id: String = if (path.startsWith("/user") || path.startsWith("/channel")) {
            path.split("/")[2]
        } else {
            throw IllegalArgumentException("Unsupported format: " + feedUrl.value)
        }
        if (path.startsWith("/channel/")) {
            return FeedId(id)
        }
        return youtubeRepository.getChannel(ChannelName(id)).orNull()
    }

    internal fun updateChannelsAndVideos(): Either<String, String> {
        logger.info("Syncing videos with youtube...")
        val playlists = channelCache.values().map { it.playlistId }

        val updatedVideos = playlists
                .map { Pair(it, videoCache[it]?.videos ?: emptyList()) }
                .map { (id, videos) ->
                    val lastUploaded = videos.sortedBy { it.uploadDate }.map { it.id }.lastOrNull()
                    val vids = youtubeRepository.getVideos(id, lastUploaded)
                    val newVids = emptyList<Video>().plus(videos).plus(vids).distinctBy { it.id }.sortedBy { it.uploadDate }
                    logger.info("Found {} new videos for {}", vids.size, id.toName())
                    if (newVids.isNotEmpty()) {
                        videoCache[id] = Videos(newVids)
                    }
                    Pair(id, Videos(newVids))
                }

        val candidatesToFetchStats = updatedVideos
                .flatMap { it.second.videos }
        val prioritized = prioritizer.prioritize(candidatesToFetchStats)

        logger.info("There are a total of {} videos", prioritized.size)

        val partitioned: List<List<ItemId>> = Lists.partition(prioritized, 50)

        var failedOnce = false
        var skippedVideos = 0
//        logger.info("Getting statistics for {} of them", videoIdsToFetchStatistics.size)
        val statistics = partitioned
                .map {
                    if (failedOnce) {
                        skippedVideos += it.size
                        return@map emptyMap<ItemId, Pair<Duration, Long>>()
                    }
                    logger.info("Getting statistics for {} videos", it.size)
                    val either = youtubeRepository.getStatistics(it)
                    when (either) {
                        is Either.Left -> {
                            logger.info("Stats failed: {}", either.a)
                            failedOnce = true
                            emptyMap()
                        }
                        is Either.Right -> either.b
                    }
                }
        logger.info("Had to skip statistics for {} videos", skippedVideos)

        val videoIdWithStatistics: Map<ItemId, Pair<Duration, Long>> = statistics
                .fold(emptyMap()) { left, right -> left.plus(right) }

        val videosWithStatistics = updateVideosWithStatistics(updatedVideos, videoIdWithStatistics)

        videosWithStatistics
                .forEach { (id, videos) -> videoCache[id] = videos }
        logger.info("Done downloading videos and statistics")
        return if (failedOnce) {
            Either.left("stats failed")
        } else {
            Either.right("OK")
        }
    }

    private fun updateVideosWithStatistics(
            videos: List<Pair<PlaylistId, Videos>>, statistics: Map<ItemId, Pair<Duration, Long>>
    ): Map<PlaylistId, Videos> {
        return videos.map { (id, videos) -> Pair(id, videos.videos) }
                .map { (id, videos) ->
                    val updatedVideos = videos.map { video ->
                        statistics[video.id]?.let { stats ->
                            video.copy(
                                    duration = stats.first,
                                    views = stats.second,
                                    lastUpdated = OffsetDateTime.now(clock)
                            )
                        } ?: video
                    }
                    Pair(id, Videos(updatedVideos))
                }
                .toMap()
    }

    private fun PlaylistId.toName() = channelCache.values().firstOrNull { it.playlistId == this }?.name
}
