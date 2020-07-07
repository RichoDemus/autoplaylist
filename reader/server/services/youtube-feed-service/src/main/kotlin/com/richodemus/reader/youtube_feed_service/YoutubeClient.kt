package com.richodemus.reader.youtube_feed_service

import arrow.core.Either
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.PlaylistId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.Optional

@Component
open class YoutubeClient(
        @Value("\${gcp.apiKey}") private val apiKey: String
) {
    private var youtube: YouTube
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        val urlOverride: Optional<String> = getUrlOverride()
        logger.info("youtube url: \"{}\"", urlOverride.orElse("Not set"))
        val builder = YouTube.Builder(
                NetHttpTransport(),
                JacksonFactory(),
                HttpRequestInitializer { request: HttpRequest? -> })
                .setApplicationName("Richo-Reader")
        urlOverride.ifPresent { rootUrl: String? -> builder.rootUrl = rootUrl }
        youtube = builder.build()
    }

    fun changeUrl(url: String) {
        val builder = YouTube.Builder(
                NetHttpTransport(),
                JacksonFactory(),
                HttpRequestInitializer { request: HttpRequest? -> })
                .setApplicationName("Richo-Reader")
        builder.rootUrl = url
        youtube = builder.build()
    }

    private fun getUrlOverride(): Optional<String> {
        val env = System.getenv("YOUTUBE_URL")
        return if (env != null) {
            Optional.of(env)
        } else Optional.ofNullable(System.getProperty("YOUTUBE_URL"))
    }

    internal open fun getChannel(id: FeedId): Either<String, List<Channel>> {
        try {
            val items = youtube
                    .channels()
                    .list("contentDetails,snippet")
                    .setKey(apiKey)
                    .setId(id.value)
                    .execute()
                    .items
                    ?: emptyList()

            return items.map { channel ->
                Channel.from(channel)
            }.let { Either.right(it) }
        } catch (e: Exception) {
            logger.error("Failed to get channel {}", id, e)
            return Either.left(e.message.orEmpty())
        }
    }

    internal open fun getId(name: ChannelName): Either<String, List<FeedId>> {
        try {
            val items = youtube
                    .channels()
                    .list("snippet,status,id,statistics")
                    .setKey(apiKey)
                    .setForUsername(name.value)
                    .execute()
                    .items
                    ?: emptyList()

            return items.map { channel ->
                Channel.from(channel)
            }.map { it.id }
                    .let { Either.right(it) }
        } catch (e: Exception) {
            logger.error("Failed to get channel {}", name, e)
            return Either.left(e.message.orEmpty())
        }
    }

    internal open fun getVideos(playlistId: PlaylistId): Sequence<Video> {
        var nextPagetoken: String? = ""
        return sequence {
            while (true) {
                try {
                    val exec = youtube.playlistItems()
                            .list("id,snippet")
                            .setKey(apiKey)
                            .setPageToken(nextPagetoken)
                            .setPlaylistId(playlistId.value)
                            .setMaxResults(50L)
                            .execute()

                    val ajtems = exec
                            .items
                            ?: emptyList()

                    nextPagetoken = exec.nextPageToken
                    val items = ajtems.map { Video.from(it) }
                    if (items.isEmpty()) {
                        break
                    }
                    yieldAll(items)
                    if (nextPagetoken == null) {
                        break
                    }
                } catch (e: Exception) {
                    logger.error("Reading videos failed for playlist {}", playlistId, e)
                    break
                }
            }
        }
    }

    internal open fun getStatistics(ids: List<ItemId>): Either<String, Map<ItemId, Pair<Duration, Long>>> {
        try {
            val idsString = ids.joinToString(",")
            val statItems = youtube.videos()
                    .list("statistics,contentDetails")
                    .setKey(apiKey)
                    .setId(idsString)
                    .execute()
                    .items

            return Either.right(statItems
                    .map { Pair(ItemId(it.id), Pair(Duration.parse(it.contentDetails.duration), getViews(it))) }
                    .toMap())
        } catch (e: Exception) {
            logger.error("Failed to get statistics", e)
            return Either.left(e.message ?: "no msg?")
        }
    }

    private fun getViews(video: com.google.api.services.youtube.model.Video): Long {
        val views = video.statistics.viewCount
        return try {
            views.longValueExact()
        } catch (e: ArithmeticException) {
            logger.error("Unable to convert view count of {} to a long for video {}", views, video.id)
            views.toLong()
        } catch (e: NullPointerException) {
            logger.warn("No view count for video {}", video.id)
            0L
        }
    }
}
