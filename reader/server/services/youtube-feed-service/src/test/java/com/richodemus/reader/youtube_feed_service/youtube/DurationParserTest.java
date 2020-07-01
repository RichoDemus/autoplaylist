package com.richodemus.reader.youtube_feed_service.youtube;

import org.junit.Test;

import java.time.Duration;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

public class DurationParserTest {
    @Test
    public void shouldParseDuration() {
        final Duration result = new DurationParser().fromYoutubeDuration("PT15M51S");

        final Duration expected = Duration.of(15, MINUTES).plus(51, SECONDS);
        assertThat(result).isEqualTo(expected);
    }
}
