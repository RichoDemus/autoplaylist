package com.richodemus.reader.youtube_feed_service

import arrow.core.Either
import arrow.core.filterOrElse
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.PlaylistId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Component
internal class YoutubeRepository(
        private val youtubeClient: YoutubeClient
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getChannel(id: FeedId): Either<String, Channel> {
        return youtubeClient.getChannel(id)
                .filterOrElse({ it.isNotEmpty() }, { "No such channel: $id" })
                .peek {
                    if ((it.size > 1)) {
                        logger.warn("More than 1 channels for id {}", id)
                    }
                }
                .map { it.first() }
    }

    fun getChannel(username: ChannelName): Either<String, FeedId> {
        return youtubeClient.getId(username)
                .filterOrElse({ it.isNotEmpty() }, { "No such channel: $username" })
                .peek {
                    if ((it.size > 1)) {
                        logger.warn("More than 1 channels for {}", username)
                    }
                }
                .map { it.first() }
    }

    fun getVideos(playlistId: PlaylistId, lastUploaded: ItemId?): List<Video> {
        return youtubeClient.getVideos(playlistId)
                .takeWhile { it.id != lastUploaded }
                .toList()
    }

    fun getStatistics(ids: List<ItemId>) = youtubeClient.getStatistics(ids)
}
