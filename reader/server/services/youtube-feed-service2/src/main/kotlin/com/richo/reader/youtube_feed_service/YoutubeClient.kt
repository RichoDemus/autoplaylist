package com.richo.reader.youtube_feed_service

import arrow.core.Either
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.PlaylistId
import org.slf4j.LoggerFactory
import java.util.Optional
import javax.inject.Named

open class YoutubeClient(
        @Named("apiKey") val apiKey: String
) {
    private val youtube: YouTube
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

    internal open fun getVideos(playlistId: PlaylistId): Sequence<Video> {
        var nextPagetoken: String? = ""
        return sequence {
            while (true) {
                try {
                    val exec = youtube.playlistItems()
                            .list("snippet")
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
}
