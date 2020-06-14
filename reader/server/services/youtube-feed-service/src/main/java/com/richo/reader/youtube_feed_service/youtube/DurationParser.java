package com.richo.reader.youtube_feed_service.youtube;

import java.time.Duration;

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
