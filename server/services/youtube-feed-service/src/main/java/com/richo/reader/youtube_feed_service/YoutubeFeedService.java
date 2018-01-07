package com.richo.reader.youtube_feed_service;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.richo.reader.youtube_feed_service.youtube.YoutubeChannelDownloader;
import com.richodemus.reader.common.kafka_adapter.EventStore;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedUrl;
import com.richodemus.reader.events_v2.UserSubscribedToFeed;

import javax.inject.Inject;
import java.util.Optional;

import static com.codahale.metrics.MetricRegistry.name;
import static com.richodemus.reader.events_v2.EventType.USER_SUBSCRIBED_TO_FEED;

public class YoutubeFeedService {
    private final FeedCache cache;
    private final YoutubeChannelDownloader youtubeChannelDownloader;
    private Timer getChannelTimer;

    @Inject
    public YoutubeFeedService(final FeedCache cache,
                              final YoutubeChannelDownloader youtubeChannelDownloader,
                              final MetricRegistry registry,
                              final EventStore eventStore) {
        this.cache = cache;
        this.youtubeChannelDownloader = youtubeChannelDownloader;
        this.getChannelTimer = registry.timer(name(YoutubeFeedService.class, "getChannel"));
        eventStore.consume(event -> {
            if (event.type() == USER_SUBSCRIBED_TO_FEED) {
                final FeedId feedId = ((UserSubscribedToFeed) event).getFeedId();
                registerChannel(feedId);
            }
            return null;
        });
    }

    private void registerChannel(final FeedId feedId) {
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
