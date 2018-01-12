package com.richo.reader.youtube_feed_service;

import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Singleton
public class FeedCache {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<FeedId, Feed> cache;
    private final JsonFileSystemPersistence fileSystemPersistence;

    @Inject
    public FeedCache(JsonFileSystemPersistence fileSystemPersistence) {
        this.fileSystemPersistence = fileSystemPersistence;
        this.cache = new HashMap<>();
    }

    public Optional<Feed> get(FeedId feedId) {
        logger.debug("Fetching channel {}", feedId);
        return Optional.ofNullable(cache.computeIfAbsent(feedId, name -> fileSystemPersistence.getChannel(feedId).orElse(null)));
    }

    public void update(Feed feed) {
        logger.debug("Updating feed: {}, {} items", feed.getId(), feed.getItems().size());
        cache.put(feed.getId(), feed);
        fileSystemPersistence.updateChannel(feed);
    }

    Set<FeedId> getAllFeedIds() {
        return cache.keySet();
    }

    void add(FeedId feedId) {
        logger.info("Adding feed {}", feedId);
        cache.putIfAbsent(feedId, new Feed(feedId, new FeedName("UNKNOWN_FEED"), new ArrayList<>(), 0L));
    }
}
