package com.richo.reader.youtube_feed_service

import arrow.core.Either
import com.google.api.client.util.DateTime
import java.time.Instant.ofEpochSecond
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

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
