package com.richo.reader.youtube_feed_service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class FeedCache
{
	private final Map<String, Feed> cache;
	private final JsonFileSystemPersistence fileSystemPersistence;

	FeedCache(JsonFileSystemPersistence fileSystemPersistence)
	{
		this.fileSystemPersistence = fileSystemPersistence;
		cache = new HashMap<>();
	}

	Optional<Feed> get(String channelName)
	{
		return Optional.ofNullable(cache.computeIfAbsent(channelName, name -> fileSystemPersistence.getChannel(channelName).orElse(null)));
	}

	void add(Feed feed)
	{
		cache.put(feed.getId(), feed);
	}
}
