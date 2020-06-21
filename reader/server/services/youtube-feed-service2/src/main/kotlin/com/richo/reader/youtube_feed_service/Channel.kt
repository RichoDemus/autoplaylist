package com.richo.reader.youtube_feed_service

import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.FeedName
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class Channel(
        val id: FeedId,
        val name: FeedName,
        val created: OffsetDateTime,
        val lastUpdated: OffsetDateTime
) {
    companion object {
        fun from(ytChannel: com.google.api.services.youtube.model.Channel): Channel {
            val id = FeedId(ytChannel.id)
            val title = FeedName(ytChannel.snippet.title)
            val publishedAt = ytChannel.snippet.publishedAt.toDate()

            return Channel(
                    id,
                    title,
                    publishedAt,
                    OffsetDateTime.now(ZoneId.of("UTC"))
            )
        }
    }
}
