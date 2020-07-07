package com.richodemus.reader.youtube_feed_service

import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.FeedName
import com.richodemus.reader.dto.PlaylistId
import java.time.OffsetDateTime
import java.time.ZoneId

data class Channel(
        val id: FeedId,
        val name: FeedName,
        val playlistId: PlaylistId,
        val created: OffsetDateTime,
        val lastUpdated: OffsetDateTime
) {
    companion object {
        fun from(ytChannel: com.google.api.services.youtube.model.Channel): Channel {
            val id = FeedId(ytChannel.id)
            val title = FeedName(ytChannel.snippet.title)
            val playlistId = PlaylistId(ytChannel.contentDetails.relatedPlaylists.uploads)
            val publishedAt = ytChannel.snippet.publishedAt.toDate()

            return Channel(
                    id,
                    title,
                    playlistId,
                    publishedAt,
                    OffsetDateTime.now(ZoneId.of("UTC"))
            )
        }
    }
}
