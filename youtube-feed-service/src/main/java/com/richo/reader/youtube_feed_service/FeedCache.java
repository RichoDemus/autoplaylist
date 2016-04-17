package com.richo.reader.youtube_feed_service;

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
	private final Map<String, Feed> cache;
	private final JsonFileSystemPersistence fileSystemPersistence;

	@Inject
	public FeedCache(JsonFileSystemPersistence fileSystemPersistence)
	{
		this.fileSystemPersistence = fileSystemPersistence;
		cache = new HashMap<>();
	}

	public Optional<Feed> get(String channelName)
	{
		logger.debug("Fetching channel {}", channelName);
		return Optional.ofNullable(cache.computeIfAbsent(channelName, name -> fileSystemPersistence.getChannel(channelName).orElse(null)));
	}

	public void update(Feed feed)
	{
		logger.debug("Updating feed: {}, {} items", feed.getId(), feed.getItems().size());
		feed.getItems().forEach(i -> logger.trace("item: {}", i));
		cache.put(feed.getId(), feed);
		fileSystemPersistence.updateChannel(feed);
	}

	public List<String> getAllFeedIds()
	{
		return fileSystemPersistence.getAllFeedIds();
	}

	public void add(String channelName)
	{
		final Feed feed = new Feed(channelName, new ArrayList<>(), 0L);
		fileSystemPersistence.updateChannel(feed);
	}
}
