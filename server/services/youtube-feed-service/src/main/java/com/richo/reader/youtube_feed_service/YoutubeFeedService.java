package com.richo.reader.youtube_feed_service;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.richo.reader.youtube_feed_service.youtube.YoutubeChannelDownloader;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedUrl;

import javax.inject.Inject;
import java.util.Optional;

import static com.codahale.metrics.MetricRegistry.name;

public class YoutubeFeedService {
    private final FeedCache cache;
    private final YoutubeChannelDownloader youtubeChannelDownloader;
    private Timer getChannelTimer;

    @Inject
    public YoutubeFeedService(final FeedCache cache,
                              final YoutubeChannelDownloader youtubeChannelDownloader,
                              final MetricRegistry registry) {
        this.cache = cache;
        this.youtubeChannelDownloader = youtubeChannelDownloader;
        this.getChannelTimer = registry.timer(name(YoutubeFeedService.class, "getChannel"));
    }

    public void registerChannel(final FeedId feedId) {
        cache.add(feedId);
    }

    public Optional<Feed> getChannel(final FeedId feedId) {
        final Timer.Context context = getChannelTimer.time();
        try {
            return cache.get(feedId);
        } finally {
            context.stop();
        }
    }

    public FeedId getFeedId(final FeedUrl feedUrl) {
        return youtubeChannelDownloader.getFeedId(feedUrl);
    }
}
