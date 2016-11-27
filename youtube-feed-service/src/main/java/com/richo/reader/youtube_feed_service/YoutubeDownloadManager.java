package com.richo.reader.youtube_feed_service;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.PlaylistItem;
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
import java.util.List;
import java.util.Optional;

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

		final List<String> itemIds = feed.getItems().stream().map(Item::getId).collect(toList());
		final List<Item> items = new ArrayList<>(feed.getItems());
		final Optional<YoutubeVideoChunk> videoChunk = youtubeChannelDownloader.getVideoChunk(feedId);

		List<PlaylistItem> nextVideoChunk;
		long itemsAddedToList = 0;
		while ((nextVideoChunk = videoChunk.get().getNextVideoChunk()).size() > 0)
		{
			logger.trace("Downloaded a chunk of {} for channel {}", nextVideoChunk.size(), feedId);
			boolean itemAlreadyInList = false;
			for (PlaylistItem item : nextVideoChunk)
			{
				if (itemIds.contains(item.getSnippet().getResourceId().getVideoId()))
				{
					itemAlreadyInList = true;
					logger.debug("Video {} is already cached, this channel should be up to date now", item);
				}
				else
				{
					items.add(toItem(item, videoChunk.get()));
					itemsAddedToList++;
				}
			}
			if (itemAlreadyInList)
			{
				break;
			}
		}
		cache.update(new Feed(feed.getId(), items, LocalDateTime.now()));
		logger.info("Downloaded {} new videos from the channel {}", itemsAddedToList, feedId);
	}

	public void updateFeedStatistics(final FeedId feedId, final ItemId itemId)
	{
		logger.info("Updating statistics for feed {}, item {}", feedId, itemId);
		cache.get(feedId).ifPresent(feed ->
		{
			final YoutubeVideoChunk youtubeVideoChunk = youtubeChannelDownloader.getVideoChunk(feedId).orElseThrow(() -> new RuntimeException("Failed to get videochunk for feed " + feedId + ", video " + itemId));

			final DurationAndViewcount durationAndViewCount = youtubeVideoChunk.getDurationAndViewCount(itemId.getId());
			final long viewCount = durationAndViewCount.viewCount;
			final Duration duration = durationAndViewCount.duration;

			final List<Item> newItems = feed.getItems().stream().map(item ->
			{
				if (item.getId().equals(itemId.getId()))
				{
					return new Item(item.getId(), item.getTitle(), item.getDescription(), item.getUploadDate(), duration, viewCount);
				}
				return item;
			}).collect(toList());

			cache.update(new Feed(feed.getId(), newItems, LocalDateTime.now()));
		});
	}

	private Item toItem(PlaylistItem playlistItem, YoutubeVideoChunk youtubeVideoChunk)
	{
		final String videoId = playlistItem.getSnippet().getResourceId().getVideoId();
		final String title = playlistItem.getSnippet().getTitle();
		final String description = playlistItem.getSnippet().getDescription();
		final LocalDateTime uploadDate = convertDate(playlistItem.getSnippet().getPublishedAt());
		final DurationAndViewcount durationAndViewCount = youtubeVideoChunk.getDurationAndViewCount(videoId);
		return new Item(videoId, title, description, uploadDate.toEpochSecond(ZoneOffset.UTC), durationAndViewCount.duration.getSeconds(), durationAndViewCount.viewCount);
	}

	private LocalDateTime convertDate(DateTime publishedAt)
	{
		final long epoch = publishedAt.getValue() / 1000;
		final int timeZoneShift = publishedAt.getTimeZoneShift();
		final int nanoSecond = 0;

		return LocalDateTime.ofEpochSecond(epoch, nanoSecond, ZoneOffset.ofHours(timeZoneShift / 60));
	}
}
