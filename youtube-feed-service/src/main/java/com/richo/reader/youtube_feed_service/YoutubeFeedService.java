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
		return getFeedWrapper(channelName)
				.map(f -> new Feed(new FeedId(f.getName().getValue()), f.getName(), f.getItems(), f.getLastUpdated()));
	}

	private Optional<Feed> getFeedWrapper(FeedId channelName)
	{
		// todo remove this workaround
		final FeedId feedId = youtubeChannelDownloader.nameToId(new FeedName(channelName.getValue()));
		final Optional<Feed> feedById = cache.get(feedId);
		if (feedById.isPresent())
		{
			return feedById;
		}
		return cache.get(channelName);
	}
}
