package com.richo.reader.youtube_feed_service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class FeedCache
{
	private final Map<String, Feed> cache;

	FeedCache()
	{
		cache = new HashMap<>();
	}

	public Optional<Feed> get(String channelName)
	{
		return Optional.ofNullable(cache.get(channelName));
	}

	public void add(Feed feed)
	{
		cache.put(feed.getId(), feed);
	}
}
