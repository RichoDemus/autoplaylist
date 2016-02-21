package com.richo.reader.youtube_feed_service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FeedCache
{
	private final Map<String, Feed> cache;
	private final JsonFileSystemPersistence fileSystemPersistence;

	public FeedCache(JsonFileSystemPersistence fileSystemPersistence)
	{
		this.fileSystemPersistence = fileSystemPersistence;
		cache = new HashMap<>();
	}

	public Optional<Feed> get(String channelName)
	{
		return Optional.ofNullable(cache.computeIfAbsent(channelName, name -> fileSystemPersistence.getChannel(channelName).orElse(null)));
	}

	public void update(Feed feed)
	{
		cache.put(feed.getId(), feed);
		fileSystemPersistence.updateChannel(feed);
	}
}
