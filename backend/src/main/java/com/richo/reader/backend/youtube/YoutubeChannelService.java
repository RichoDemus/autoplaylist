package com.richo.reader.backend.youtube;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.PlaylistItem;
import com.richo.reader.backend.youtube.cache.YoutubeChannelCache;
import com.richo.reader.backend.youtube.download.YouTubeVideoChuck;
import com.richo.reader.backend.youtube.download.YoutubeChannelDownloader;
import com.richo.reader.backend.youtube.model.YoutubeChannel;
import com.richo.reader.backend.youtube.model.YoutubeVideo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
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
	private final Duration channelAgeUntilFrefresh;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final YoutubeChannelDownloader youtubeChannelDownloader;
	private final YoutubeChannelCache cache;

	public YoutubeChannelService(YoutubeChannelDownloader youtubeChannelDownloader, YoutubeChannelCache cache, Duration channelAgeUntilFrefresh)
	{
		this.channelAgeUntilFrefresh = channelAgeUntilFrefresh;
		this.youtubeChannelDownloader = youtubeChannelDownloader;
		this.cache = cache;
	}

	public Optional<YoutubeChannel> getChannelByName(String channelName)
	{
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
			items.addAll(nextVideoChunk);
		}

		final Set<YoutubeVideo> videos = items.stream().map(this::toVideo).collect(Collectors.toSet());
		final YoutubeChannel youtubeChannel = new YoutubeChannel(channelName, videos);
		cache.updateChannel(youtubeChannel);
		return Optional.of(youtubeChannel);

	}

	private Optional<YoutubeChannel> getChannelFromCacheIfNotOutdated(String channelName, YoutubeChannelCache cache)
	{
		return cache.getChannel(channelName).filter(this::outdatedChannel);
	}

	private boolean outdatedChannel(YoutubeChannel channel)
	{
		return Duration.between(channel.getLastUpdated(), Instant.now()).compareTo(channelAgeUntilFrefresh) < 0;
	}

	private YoutubeVideo toVideo(PlaylistItem playlistItem)
	{
		final String videoId = playlistItem.getSnippet().getResourceId().getVideoId();
		final String urlString = "https://www.youtube.com/watch?v=" + videoId;
		final URL url;
		try
		{
			url = new URL(urlString);
		}
		catch (MalformedURLException e)
		{
			logger.error("{} is not a valid url", urlString, e);
			return null;
		}
		return new YoutubeVideo(playlistItem.getSnippet().getTitle(), playlistItem.getSnippet().getDescription(), url, convertDate(playlistItem.getSnippet().getPublishedAt()));
	}

	private LocalDateTime convertDate(DateTime publishedAt)
	{
		final long epoch = publishedAt.getValue() / 1000;
		final int timeZoneShift = publishedAt.getTimeZoneShift();
		final int nanoSecond = 0;

		return LocalDateTime.ofEpochSecond(epoch, nanoSecond, ZoneOffset.ofHours(timeZoneShift / 60));
	}
}
