package com.richodemus.reader.youtube_feed_service

import java.time.Clock
import java.time.ZoneId

class PassthroughPrioritizer : StatisticsUpdatePrioritizer(Clock.fixed(date("2020-01-04").toInstant(), ZoneId.of("UTC"))) {
    override fun prioritize(videos: List<Video>) = videos.map { it.id }
}
