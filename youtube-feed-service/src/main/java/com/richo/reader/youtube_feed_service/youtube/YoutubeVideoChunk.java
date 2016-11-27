package com.richo.reader.youtube_feed_service.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoContentDetails;
import com.google.api.services.youtube.model.VideoStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//todo this should use the Iterator-pattern instead,
// it's pretty much similiar except you can probably do functional stuff or observables on it
public class YoutubeVideoChunk
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final DurationParser durationParser;
	private final YouTube youtube;
	private final String playlistId;
	private final String apiKey;

	private Optional<String> nextPageToken;

	YoutubeVideoChunk(YouTube youtube, DurationParser durationParser, String playlistId, String apiKey)
	{
		this.youtube = youtube;
		this.durationParser = durationParser;
		this.playlistId = playlistId;
		this.apiKey = apiKey;
		nextPageToken = Optional.of("");
	}

	public List<PlaylistItem> getNextVideoChunk()
	{
		if (!nextPageToken.isPresent())
		{
			logger.debug("No more channels for playlistId {}", playlistId);
			return new ArrayList<>();
		}

		final PlaylistItemListResponse playlistItemListResponse;
		try
		{
			playlistItemListResponse = youtube.playlistItems()
					.list("snippet")
					.setKey(apiKey)
					.setPageToken(nextPageToken.get())
					.setPlaylistId(playlistId)
					.setMaxResults(50L)
					.execute();
		}
		catch (IOException e)
		{
			logger.error("Unable to get page {} of playlist {}", nextPageToken, playlistId, e);
			return new ArrayList<>();
		}
		nextPageToken = Optional.ofNullable(playlistItemListResponse.getNextPageToken());
		return playlistItemListResponse.getItems();
	}

	public DurationAndViewcount getDurationAndViewCount(final String itemId)
	{
		final List<Video> items;
		try
		{
			items = youtube.videos()
					.list("snippet,status,id,statistics,contentDetails")
					.setKey(apiKey)
					.setId(itemId)
					.execute()
					.getItems();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		Long viewCount;
		try
		{
			viewCount = items.stream()
					.map(Video::getStatistics)
					.map(VideoStatistics::getViewCount)
					.map(BigInteger::longValueExact)
					.findAny()
					.orElseThrow(() -> new RuntimeException("Unable to get view count for video " + itemId));
		}
		catch (ArithmeticException e)
		{
			final BigInteger count = items.stream().map(Video::getStatistics).map(VideoStatistics::getViewCount).findAny().orElseThrow(() -> new RuntimeException("This pretty much can't happen, itemId " + itemId, e));
			logger.error("Unable to convert view count of {} to a long for video {}", count, itemId);
			viewCount = 0L;
		}
		final Duration duration = items.stream()
				.map(Video::getContentDetails)
				.map(VideoContentDetails::getDuration)
				.map(durationParser::fromYoutubeDuration)
				.findAny()
				.orElseThrow(() -> new RuntimeException("Unable to get duration for video " + itemId));
		return new DurationAndViewcount(duration, viewCount);
	}
}
