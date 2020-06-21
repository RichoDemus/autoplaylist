package com.richo.reader.youtube_feed_service

import com.richodemus.reader.dto.ItemId
import java.time.Duration
import java.time.ZonedDateTime

data class Video(
        val id: ItemId,
        val title: String,
        val description: String,
        val uploadDate: ZonedDateTime,
        val lastUpdated: ZonedDateTime,
        val duration: Duration,
        val views: Long
)
