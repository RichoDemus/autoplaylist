package com.richo.reader.youtube_feed_service;

import com.richo.reader.youtube_feed_service.youtube.YoutubeChannelDownloader;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedUrl;

import javax.inject.Inject;
import java.util.Optional;

public class YoutubeFeedService
{
	private final FeedCache cache;
	private final YoutubeChannelDownloader youtubeChannelDownloader;

	@Inject
	public YoutubeFeedService(FeedCache cache, YoutubeChannelDownloader youtubeChannelDownloader)
	{
		this.cache = cache;
		this.youtubeChannelDownloader = youtubeChannelDownloader;
	}

	public void registerChannel(final FeedId feedId)
	{
		cache.add(feedId);
	}

	public Optional<Feed> getChannel(final FeedId feedId)
	{
		return cache.get(feedId);
	}

	public FeedId getFeedId(final FeedUrl feedUrl)
	{
		return youtubeChannelDownloader.getFeedId(feedUrl);
	}
}
