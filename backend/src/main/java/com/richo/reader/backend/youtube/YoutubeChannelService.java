package com.richo.reader.backend.youtube;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.PlaylistItem;
import com.richo.reader.backend.model.Feed;
import com.richo.reader.backend.model.Item;
import com.richo.reader.backend.persistence.YoutubeChannelPersistence;
import com.richo.reader.backend.youtube.download.YoutubeChannelDownloader;
import com.richo.reader.backend.youtube.download.YoutubeVideoChunk;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

public class YoutubeChannelService
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Duration channelAgeUntilFrefresh;
	private final YoutubeChannelDownloader youtubeChannelDownloader;
	private final YoutubeChannelPersistence cache;
	private final Lock downloadLock;

	@Inject
	public YoutubeChannelService(YoutubeChannelDownloader youtubeChannelDownloader, YoutubeChannelPersistence cache, Duration channelAgeUntilFrefresh)
	{
		this.channelAgeUntilFrefresh = channelAgeUntilFrefresh;
		this.youtubeChannelDownloader = youtubeChannelDownloader;
		this.cache = cache;
		this.downloadLock = new ReentrantLock();
	}

	public Optional<Feed> getFeedById(String feedId)
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
		//Todo this is a temporary solution, fix
		downloadLock.lock();
		try
		{
			return getChannelByNameInner(channelName);
		}
		finally
		{
			downloadLock.unlock();
		}
	}

	public Optional<YoutubeChannel> getChannelByNameInner(String channelName)
	{
		//todo refactor this method, it's balls
		logger.info("Channel {} requested", channelName);
		final Optional<YoutubeChannel> maybeChannelFromCache = cache.getChannel(channelName);
		final Optional<YoutubeChannel> channelFromCacheUpToDate = maybeChannelFromCache
				.filter(this::outdatedChannel);
		if (channelFromCacheUpToDate.isPresent())
		{
			return channelFromCacheUpToDate;
		}
		logger.debug("Up to date version of channel {} not found in cache, downloading", channelName);

		final Optional<YoutubeVideoChunk> videoChunk = youtubeChannelDownloader.getVideoChunk(channelName);
		if (!videoChunk.isPresent())
		{
			logger.debug("No such channel {}", channelName);
			return Optional.empty();
		}

		final List<YoutubeVideo> videos = maybeChannelFromCache
				.map(YoutubeChannel::getVideos)
				.map((Function<Set<YoutubeVideo>, ArrayList<YoutubeVideo>>) ArrayList::new)
				.orElseGet(ArrayList::new);

		List<PlaylistItem> nextVideoChunk;
		long itemsAddedToList = 0;
		while ((nextVideoChunk = videoChunk.get().getNextVideoChunk()).size() > 0)
		{
			logger.trace("Downloaded a chunk of {} for channel {}", nextVideoChunk.size(), channelName);
			boolean itemAlreadyInList = false;
			for (PlaylistItem item : nextVideoChunk)
			{
				final YoutubeVideo video = toVideo(item);
				if (videos.contains(video))
				{
					itemAlreadyInList = true;
				}
				else
				{
					videos.add(video);
					itemsAddedToList++;
				}
			}
			if (itemAlreadyInList)
			{
				break;
			}
		}
		logger.debug("Downloaded {} new videos from the channel {}", itemsAddedToList, channelName);
		final YoutubeChannel youtubeChannel = new YoutubeChannel(channelName, videos);
		cache.updateChannel(youtubeChannel);
		return Optional.of(youtubeChannel);

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
