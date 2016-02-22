package com.richo.reader.youtube_feed_service.youtube;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.PlaylistItem;
import com.richo.reader.youtube_feed_service.Feed;
import com.richo.reader.youtube_feed_service.FeedCache;
import com.richo.reader.youtube_feed_service.Item;
import com.richo.reader.youtube_feed_service.youtube.download.YoutubeChannelDownloader;
import com.richo.reader.youtube_feed_service.youtube.download.YoutubeVideoChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
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

	public void downloadFeed(String channelName)
	{
		//todo refactor this method, it's balls
		logger.info("Channel {} requested", channelName);
		final Feed feed = cache.get(channelName).orElse(new Feed(channelName, new ArrayList<>(), LocalDateTime.now()));

		final List<String> itemIds = feed.getItems().stream().map(Item::getId).collect(toList());
		final List<Item> items = new ArrayList<>(feed.getItems());
		final Optional<YoutubeVideoChunk> videoChunk = youtubeChannelDownloader.getVideoChunk(channelName);

		List<PlaylistItem> nextVideoChunk;
		long itemsAddedToList = 0;
		while ((nextVideoChunk = videoChunk.get().getNextVideoChunk()).size() > 0)
		{
			logger.trace("Downloaded a chunk of {} for channel {}", nextVideoChunk.size(), channelName);
			boolean itemAlreadyInList = false;
			for (PlaylistItem item : nextVideoChunk)
			{
				if(itemIds.contains(item.getSnippet().getResourceId().getVideoId()))
				{
					itemAlreadyInList = true;
					logger.debug("Video {} is already cached, this channel should be up to date now", item);
				}
				else
				{
					items.add(toItem(item));
					itemsAddedToList++;
				}
			}
			if (itemAlreadyInList)
			{
				break;
			}
		}
		logger.debug("Downloaded {} new videos from the channel {}", itemsAddedToList, channelName);
		cache.update(new Feed(feed.getId(), items, LocalDateTime.now()));
	}

	private Item toItem(PlaylistItem playlistItem)
	{
		final String videoId = playlistItem.getSnippet().getResourceId().getVideoId();
		final String title = playlistItem.getSnippet().getTitle();
		final String description = playlistItem.getSnippet().getDescription();
		final LocalDateTime uploadDate = convertDate(playlistItem.getSnippet().getPublishedAt());
		return new Item(videoId, title, description, uploadDate.toEpochSecond(ZoneOffset.UTC));
	}

	private LocalDateTime convertDate(DateTime publishedAt)
	{
		final long epoch = publishedAt.getValue() / 1000;
		final int timeZoneShift = publishedAt.getTimeZoneShift();
		final int nanoSecond = 0;

		return LocalDateTime.ofEpochSecond(epoch, nanoSecond, ZoneOffset.ofHours(timeZoneShift / 60));
	}
}
