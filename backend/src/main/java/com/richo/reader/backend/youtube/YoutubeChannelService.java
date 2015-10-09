package com.richo.reader.backend.youtube;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.PlaylistItem;
import com.richo.reader.backend.model.Feed;
import com.richo.reader.backend.model.Item;
import com.richo.reader.backend.persistence.YoutubeChannelPersistence;
import com.richo.reader.backend.youtube.download.YouTubeVideoChuck;
import com.richo.reader.backend.youtube.download.YoutubeChannelDownloader;
import com.richo.reader.backend.youtube.model.YoutubeChannel;
import com.richo.reader.backend.youtube.model.YoutubeVideo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class YoutubeChannelService
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Duration channelAgeUntilFrefresh;
	private final YoutubeChannelDownloader youtubeChannelDownloader;
	private final YoutubeChannelPersistence cache;

	@Inject
	public YoutubeChannelService(YoutubeChannelDownloader youtubeChannelDownloader, YoutubeChannelPersistence cache, Duration channelAgeUntilFrefresh)
	{
		this.channelAgeUntilFrefresh = channelAgeUntilFrefresh;
		this.youtubeChannelDownloader = youtubeChannelDownloader;
		this.cache = cache;
	}

	public Optional<Feed>  getFeedById(String feedId)
	{
		return getFeedByName(feedId);
	}

	public Optional<Feed> getFeedByName(final String feedName)
	{
		return getChannelByName(feedName).map(this::toFeed);
	}

	private Feed toFeed(YoutubeChannel channel)
	{
		final String id = channel.getName();
		final Feed feed = new Feed(id, id);
		feed.addNewItems(channel.getVideos()
				.stream()
				.map(this::videoToItem)
				.collect(Collectors.toSet()));
		return feed;
	}

	private Item videoToItem(YoutubeVideo video)
	{
		return new Item(video.getVideoId(), video.getTitle(), video.getDescription(), video.getUrl(), video.getUploadDate());
	}

	public Optional<YoutubeChannel> getChannelByName(String channelName)
	{
		logger.info("Channel {} requested", channelName);
		final Optional<YoutubeChannel> channelFromCache = getChannelFromCacheIfNotOutdated(channelName, cache);
		if (channelFromCache.isPresent())
		{
			return channelFromCache;
		}
		logger.debug("Channel {} not found in cache, downloading", channelName);

		final Optional<YouTubeVideoChuck> videoChunk = youtubeChannelDownloader.getVideoChunk(channelName);
		if (!videoChunk.isPresent())
		{
			logger.debug("No such channel {}", channelName);
			return Optional.empty();
		}

		final List<PlaylistItem> items = new ArrayList<>();
		List<PlaylistItem> nextVideoChunk;
		while ((nextVideoChunk = videoChunk.get().getNextVideoChunk()).size() > 0)
		{
			logger.trace("Downloaded a chunk of {} for channel {}", nextVideoChunk.size(), channelName);
			items.addAll(nextVideoChunk);
		}
		logger.debug("Downloaded {} videos from the channel {}", items.size(), channelName);
		final Set<YoutubeVideo> videos = items.stream().map(this::toVideo).filter(this::nullItems).collect(Collectors.toSet());
		final YoutubeChannel youtubeChannel = new YoutubeChannel(channelName, videos);
		cache.updateChannel(youtubeChannel);
		return Optional.of(youtubeChannel);

	}

	private boolean nullItems(YoutubeVideo youtubeVideo)
	{
		return youtubeVideo != null;
	}

	private Optional<YoutubeChannel> getChannelFromCacheIfNotOutdated(String channelName, YoutubeChannelPersistence cache)
	{
		return cache.getChannel(channelName)
				.filter(this::outdatedChannel);
	}

	private boolean outdatedChannel(YoutubeChannel channel)
	{
		if (Duration.between(channel.getLastUpdated(), Instant.now()).compareTo(channelAgeUntilFrefresh) < 0)
		{
			return true;
		}
		logger.debug("Channel {} is outdated", channel.getName());
		return false;
	}

	private YoutubeVideo toVideo(PlaylistItem playlistItem)
	{
		final String videoId = playlistItem.getSnippet().getResourceId().getVideoId();
		return new YoutubeVideo(playlistItem.getSnippet().getTitle(), playlistItem.getSnippet().getDescription(), videoId, convertDate(playlistItem.getSnippet().getPublishedAt()));
	}

	private LocalDateTime convertDate(DateTime publishedAt)
	{
		final long epoch = publishedAt.getValue() / 1000;
		final int timeZoneShift = publishedAt.getTimeZoneShift();
		final int nanoSecond = 0;

		return LocalDateTime.ofEpochSecond(epoch, nanoSecond, ZoneOffset.ofHours(timeZoneShift / 60));
	}
}
