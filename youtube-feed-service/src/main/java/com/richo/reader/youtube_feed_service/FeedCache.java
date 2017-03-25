package com.richo.reader.youtube_feed_service;

import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class FeedCache
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Map<FeedId, Feed> cache;
	private final JsonFileSystemPersistence fileSystemPersistence;

	@Inject
	public FeedCache(JsonFileSystemPersistence fileSystemPersistence)
	{
		this.fileSystemPersistence = fileSystemPersistence;
		this.cache = new HashMap<>();
	}

	public Optional<Feed> get(FeedId feedId)
	{
		logger.debug("Fetching channel {}", feedId);
		return Optional.ofNullable(cache.computeIfAbsent(feedId, name -> fileSystemPersistence.getChannel(feedId).orElse(null)));
	}

	public void update(Feed feed)
	{
		logger.debug("Updating feed: {}, {} items", feed.getId(), feed.getItems().size());
		feed.getItems().forEach(i -> logger.trace("item: {}", i));
		cache.put(feed.getId(), feed);
		fileSystemPersistence.updateChannel(feed);
	}

	List<FeedId> getAllFeedIds()
	{
		return fileSystemPersistence.getAllFeedIds();
	}

	public void add(FeedId feedId)
	{
		final Feed feed = new Feed(feedId, new FeedName("UNKNOWN_FEED"), new ArrayList<>(), 0L);
		fileSystemPersistence.updateChannel(feed);
	}
}
