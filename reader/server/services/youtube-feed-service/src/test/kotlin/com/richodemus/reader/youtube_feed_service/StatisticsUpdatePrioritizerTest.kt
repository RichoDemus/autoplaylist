package com.richodemus.reader.youtube_feed_service

import com.richodemus.reader.dto.ItemId
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Clock
import java.time.Duration
import java.time.ZoneId

class StatisticsUpdatePrioritizerTest {
    @Test
    fun `Should order videos for statistics`() {
        val target = StatisticsUpdatePrioritizer(Clock.fixed(date("2020-01-04").toInstant(), ZoneId.of("UTC")))

        val result = target.prioritize(listOf(
                Video(ItemId("uploaded recently"), "title1", "desc1", date("2020-01-01"), date("2020-01-01"), Duration.ZERO, 0),
                Video(ItemId("last updated over 2 months ago"), "title2", "desc2", date("2019-01-01"), date("2019-01-02"), Duration.ZERO, 0),
                Video(ItemId("uploaded same day of week"), "title3", "desc3", date("2019-06-04"), date("2020-01-01"), Duration.ZERO, 0),
                Video(ItemId("should not be updated"), "title4", "desc4", date("2019-12-20"), date("2019-12-30"), Duration.ZERO, 0)
        ))

        assertThat(result).containsExactly(ItemId("uploaded recently"), ItemId("last updated over 2 months ago"), ItemId("uploaded same day of week"))
    }
}
