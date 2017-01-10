package com.richo.reader.youtube_feed_service;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.common.collect.Lists;
import com.richo.reader.youtube_feed_service.youtube.DurationAndViewcount;
import com.richo.reader.youtube_feed_service.youtube.YoutubeChannelDownloader;
import com.richo.reader.youtube_feed_service.youtube.YoutubeVideoChunk;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.ItemId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class YoutubeDownloadManager
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final YoutubeChannelDownloader youtubeChannelDownloader;
	private final FeedCache cache;

	@Inject
	public YoutubeDownloadManager(YoutubeChannelDownloader youtubeChannelDownloader, FeedCache cache)
	{
		this.youtubeChannelDownloader = youtubeChannelDownloader;
		this.cache = cache;
	}

	public void downloadFeed(FeedId feedId)
	{
		//todo refactor this method, it's balls
		logger.info("Channel {} requested", feedId);
		final Feed feed = cache.get(feedId).orElse(new Feed(feedId, new ArrayList<>(), LocalDateTime.now()));

		final List<ItemId> itemIds = feed.getItems().stream().map(Item::getId).collect(toList());
		final List<Item> items = new ArrayList<>(feed.getItems());
		final Optional<YoutubeVideoChunk> videoChunk = youtubeChannelDownloader.getVideoChunk(feedId);

		if (!videoChunk.isPresent())
		{
			logger.warn("Couldn't find channel {}", feed);
			return;
		}

		List<PlaylistItem> nextVideoChunk;
		final List<Item> itemsToAdd = new ArrayList<>();
		while ((nextVideoChunk = videoChunk.get().getNextVideoChunk()).size() > 0)
		{
			logger.info("Downloaded a chunk of {} for channel {}, {} in total", nextVideoChunk.size(), feedId, itemsToAdd.size());
			boolean itemAlreadyInList = false;
			for (PlaylistItem item : nextVideoChunk)
			{
				if (itemIds.contains(new ItemId(item.getSnippet().getResourceId().getVideoId())))
				{
					itemAlreadyInList = true;
					logger.debug("Video {} is already cached, this channel should be up to date now", item);
				}
				else
				{
					itemsToAdd.add(toItem(item));
				}
			}
			if (itemAlreadyInList)
			{
				break;
			}
		}

		if (!itemsToAdd.isEmpty())
		{
			logger.info("Getting statistics for {} new videos in feed {}", itemsToAdd.size(), feedId);
		}
		final List<Item> itemsToAddWithStatistics = addStatistics(itemsToAdd);

		itemsToAddWithStatistics.forEach(items::add);
		cache.update(new Feed(feed.getId(), items, LocalDateTime.now()));
		logger.info("Downloaded {} new videos from the channel {}", itemsToAdd.size(), feedId);
	}

	public void updateFeedStatistics(final FeedId feedId)
	{
		logger.info("Updating statistics for feed {}", feedId);
		final Feed feed = cache.get(feedId).orElseThrow(() -> new RuntimeException("No such feed: " + feedId));

		final List<Item> newItems = addStatistics(feed.getItems());

		cache.update(new Feed(feed.getId(), newItems, LocalDateTime.now()));
	}

	private List<Item> addStatistics(List<Item> itemsToAdd)
	{
		return Lists.partition(itemsToAdd, 50).stream()
				.map(this::toItemWithStatistics)
				.flatMap(Collection::stream)
				.collect(toList());
	}

	private List<Item> toItemWithStatistics(List<Item> items)
	{
		logger.info("Getting statistics for {} items", items.size());
		final String ids = items.stream().map(Item::getId).map(ItemId::getValue).collect(joining(","));
		final Map<ItemId, DurationAndViewcount> statistics = youtubeChannelDownloader.getStatistics(ids);
		return items.stream()
				.map(item ->
				{
					final DurationAndViewcount durationAndViewcount = statistics.get(item.getId());
					if (durationAndViewcount != null)
					{
						return new Item(item.getId(),
								item.getTitle(),
								item.getDescription(),
								item.getUploadDate(),
								item.getAdded(),
								durationAndViewcount.duration,
								durationAndViewcount.viewCount);
					}

					final Map<ItemId, DurationAndViewcount> newStatistics = youtubeChannelDownloader.getStatistics(item.getId());
					final boolean unavailable = newStatistics.isEmpty();
					if (unavailable)
					{
						logger.info("Video {}({}) is unavailable", item.getTitle(), item.getId());
						return new Item(item.getId(), item.getTitle(), item.getDescription(), item.getUploadDate(), item.getAdded(), item.getDuration(), item.getViews());
					}
					else
					{
						logger.warn("Had to retry {}({})", item.getTitle(), item.getId());
						final DurationAndViewcount newDurationAndViewCount = newStatistics.get(item.getId());
						return new Item(item.getId(), item.getTitle(), item.getDescription(), item.getUploadDate(), item.getAdded(), newDurationAndViewCount.duration, newDurationAndViewCount.viewCount);
					}
				})
				.collect(toList());
	}

	private Item toItem(PlaylistItem playlistItem)
	{
		final String videoId = playlistItem.getSnippet().getResourceId().getVideoId();
		final String title = playlistItem.getSnippet().getTitle();
		final String description = playlistItem.getSnippet().getDescription();
		final LocalDateTime uploadDate = convertDate(playlistItem.getSnippet().getPublishedAt());
		return new Item(videoId, title, description, uploadDate.toEpochSecond(UTC), LocalDateTime.now().toEpochSecond(UTC), Duration.ZERO.toMillis(), 0L);
	}

	private LocalDateTime convertDate(DateTime publishedAt)
	{
		final long epoch = publishedAt.getValue() / 1000;
		final int timeZoneShift = publishedAt.getTimeZoneShift();
		final int nanoSecond = 0;

		return LocalDateTime.ofEpochSecond(epoch, nanoSecond, ZoneOffset.ofHours(timeZoneShift / 60));
	}
}
