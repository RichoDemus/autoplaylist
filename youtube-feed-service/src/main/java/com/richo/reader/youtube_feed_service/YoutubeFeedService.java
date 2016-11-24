package com.richo.reader.youtube_feed_service;

import com.richodemus.reader.dto.FeedId;

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

	public void registerChannel(final FeedId channelName)
	{
		cache.add(channelName);
	}

	public Optional<Feed> getChannel(final FeedId channelName)
	{
		return cache.get(channelName);
	}
}
