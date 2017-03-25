package com.richo.reader.youtube_feed_service;

import com.richo.reader.youtube_feed_service.youtube.YoutubeChannelDownloader;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedName;

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

	public void registerChannel(final FeedId channelName)
	{
		cache.add(channelName);
	}

	public Optional<Feed> getChannel(final FeedId channelName)
	{
		return getFeedWrapper(channelName);
	}

	private Optional<Feed> getFeedWrapper(FeedId channelName)
	{
		// todo remove this workaround
		FeedId feedId;
		try
		{
			feedId = youtubeChannelDownloader.nameToId(new FeedName(channelName.getValue()));
		}
		catch (Exception e)
		{
			// if this fails, it probably means that FeedId is actually a feedId and not a name
			feedId = channelName;
		}
		final Optional<Feed> feedById = cache.get(feedId);
		if (feedById.isPresent())
		{
			return feedById;
		}
		return cache.get(channelName);
	}
}
