package com.richodemus.reader.youtube_feed_service.youtube;

import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
class DurationParser {
    /**
     * Parses the youtube duration into java Java Duration
     *
     * @param duration ISO 8601 duration in the format PT#M#S
     * @return java8 Duration
     */
    Duration fromYoutubeDuration(final String duration) {
        return Duration.parse(duration);
    }
}
