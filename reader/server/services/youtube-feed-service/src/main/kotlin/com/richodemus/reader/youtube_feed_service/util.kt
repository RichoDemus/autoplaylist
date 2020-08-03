package com.richodemus.reader.youtube_feed_service

import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue
import com.google.api.client.util.DateTime
import java.time.Instant.ofEpochSecond
import java.time.OffsetDateTime
import java.time.ZoneOffset

internal fun DateTime.toDate(): OffsetDateTime {
    val epoch = this.value / 1000
    val timeZoneShift = this.timeZoneShift
    return OffsetDateTime.ofInstant(ofEpochSecond(epoch), ZoneOffset.ofHours(timeZoneShift / 60))
}

internal fun <A, B> Either<A, B>.peek(function: (B) -> Unit): Either<A, B> {
    return this.map {
        function(it)
        it
    }
}

data class ChannelName(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "FeedId can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}

internal fun shouldFetchStatistics(video: Video, now: OffsetDateTime): Boolean {
    if (video.uploadDate.isAfter(now.minusDays(7))) {
        return true
    }
    if (video.uploadDate.dayOfMonth == now.dayOfMonth) {
        return true
    }
    return false
}

/**
 * Format: YYYY-MM-DD
 */
internal fun date(str: String): OffsetDateTime {
    val split = str.split("-")
    val year = split[0].toInt()
    val month = split[1].toInt()
    val day = split[2].toInt()
    return OffsetDateTime.of(year, month, day, 0, 0, 0, 0, ZoneOffset.UTC)
}
