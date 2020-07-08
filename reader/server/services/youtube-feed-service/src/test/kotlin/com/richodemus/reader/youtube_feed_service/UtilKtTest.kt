package com.richodemus.reader.youtube_feed_service

import com.richodemus.reader.dto.ItemId
import date
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Duration
import java.time.OffsetDateTime

class UtilKtTest {
    @Test
    fun `Should only get stats for older videos once per month`() {
        assertThat(shouldFetchStatistics(vid("2020-01-01"), date("2020-01-02"))).isTrue()
        assertThat(shouldFetchStatistics(vid("2020-01-01"), date("2020-01-07"))).isTrue()
        assertThat(shouldFetchStatistics(vid("2020-01-01"), date("2020-01-10"))).isFalse()
        assertThat(shouldFetchStatistics(vid("2020-01-03"), date("2020-02-03"))).isTrue()
    }

    private fun vid(dateStr: String) = Video(ItemId("asd"), "", "", date(dateStr), OffsetDateTime.MIN, Duration.ZERO, 0)
}