package com.richo.reader.youtube_feed_service;

import javax.inject.Inject;
import java.util.Optional;

public class YoutubeFeedService
{
	private final FeedCache cache;

	@Inject
	public YoutubeFeedService(FeedCache cache)
	{
		this.cache = cache;
	}

	public void registerChannel(final String channelName)
	{

	}

	public Optional<Feed> getChannel(final String channelName)
	{
		return cache.get(channelName);
	}
}
