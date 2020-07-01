package com.richodemus.reader.youtube_feed_service.youtube;

import java.time.Duration;

public class DurationAndViewcount {
    public final Duration duration;
    public final long viewCount;

    public DurationAndViewcount(final Duration duration, final long viewCount) {
        this.duration = duration;
        this.viewCount = viewCount;
    }
}
