package com.richodemus.reader.youtube_feed_service

import com.google.api.services.youtube.model.PlaylistItem
import com.richodemus.reader.dto.ItemId
import java.time.Duration
import java.time.OffsetDateTime

data class Video(
        val id: ItemId,
        val title: String,
        val description: String,
        val uploadDate: OffsetDateTime,
        val lastUpdated: OffsetDateTime,
        val duration: Duration,
        val views: Long
) {
    companion object {
        fun from(playlistItem: PlaylistItem): Video {
            val id = ItemId(playlistItem.snippet.resourceId.videoId)
            val title = playlistItem.snippet.title
            val description = playlistItem.snippet.description
            val publishedAt = playlistItem.snippet.publishedAt.toDate()

            return Video(
                    id,
                    title,
                    description,
                    publishedAt,
                    date("1970-01-01"),
                    Duration.ZERO,
                    0L
            )
        }
    }
}
