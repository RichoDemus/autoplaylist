package com.richodemus.reader.youtube_feed_service

import com.richodemus.reader.dto.ItemId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.OffsetDateTime

@Component
open class StatisticsUpdatePrioritizer(
        private val clock: Clock
) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)

    open fun prioritize(videos: List<Video>): List<ItemId> {
        val now = OffsetDateTime.now(clock)

        val (newVideos, olderVideos) = videos.partition { it.uploadDate.isAfter(now.minusDays(7)) }
        val (moreThanTwoMonthsSinceUpdate, lessThanTwoMonthsSinceUpdate) = olderVideos.partition { it.lastUpdated.isBefore(now.minusMonths(2)) }
        val (uploadDaySameAsCurrentDay, restOfVideos) = lessThanTwoMonthsSinceUpdate.partition { it.uploadDate.dayOfMonth == now.dayOfMonth }

        val allVideos = newVideos + moreThanTwoMonthsSinceUpdate + uploadDaySameAsCurrentDay
        logger.info("There are ${newVideos.size} new videos, ${moreThanTwoMonthsSinceUpdate.size} last updated over 2 months ago ${uploadDaySameAsCurrentDay.size} with same weekday, for a total of ${allVideos.size}")
        return allVideos.map { it.id }
    }
}
