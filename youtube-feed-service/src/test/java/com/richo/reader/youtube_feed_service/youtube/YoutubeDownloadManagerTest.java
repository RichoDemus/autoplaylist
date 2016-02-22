package com.richo.reader.youtube_feed_service.youtube;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.ResourceId;
import com.richo.reader.youtube_feed_service.Feed;
import com.richo.reader.youtube_feed_service.FeedCache;
import com.richo.reader.youtube_feed_service.Item;
import com.richo.reader.youtube_feed_service.JsonFileSystemPersistence;
import com.richo.reader.youtube_feed_service.youtube.download.YoutubeChannelDownloader;
import com.richo.reader.youtube_feed_service.youtube.download.YoutubeVideoChunk;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class YoutubeDownloadManagerTest
{
	private static final String NON_EXISTENT_CHANNEL_NAME = "non existerino";
	private static final Item CACHED_CHANNEL_FIRST_VIDEO = new Item("_0S1jebDBzk", "cached_1", "description1", LocalDateTime.of(2014, 9, 5, 12, 37, 56));
	private static final Feed CACHED_CHANNEL = new Feed(
			"cached_channel",
			singletonList(CACHED_CHANNEL_FIRST_VIDEO),
			LocalDateTime.now());

	private static final Item UNCACHED_CHANNEL_FIRST_VIDEO = new Item("_0S1jebDBzk", "uncached_1", "description1", LocalDateTime.of(2014, 9, 5, 12, 37, 56));
	private static final Feed UNCACHED_CHANNEL = new Feed(
			"uncached_channel",
			singletonList(UNCACHED_CHANNEL_FIRST_VIDEO),
			LocalDateTime.now());

	private static final Item OUTDATED_CHANNEL_FIRST_VIDEO = new Item("_0S1jebDBzk", "outdated_1", "description1", LocalDateTime.of(2014, 9, 5, 12, 37, 56));
	private static final Item OUTDATED_CHANNEL_SECOND_VIDEO = new Item("_0S1jebdDBzk", "outdated_2", "description2", LocalDateTime.of(2014, 9, 10, 12, 37, 56));
	private static final Item OUTDATED_CHANNEL_NOT_CACHED_VIDEO = new Item("_0s1jebDBze", "outdated_noncached_3", "description3", LocalDateTime.of(2014, 11, 5, 12, 37, 56));
	private static final Feed OUTDATED_CHANNEL_WITH_NEW_ITEM = new Feed(
			"outdated_channel",
			asList(OUTDATED_CHANNEL_FIRST_VIDEO, OUTDATED_CHANNEL_SECOND_VIDEO, OUTDATED_CHANNEL_NOT_CACHED_VIDEO),
			LocalDateTime.ofEpochSecond(100L, 0, ZoneOffset.UTC));
	private static final Feed OUTDATED_CHANNEL_WITHOUT_NEW_ITEM = new Feed(
			"outdated_channel",
			asList(OUTDATED_CHANNEL_FIRST_VIDEO, OUTDATED_CHANNEL_SECOND_VIDEO),
			LocalDateTime.ofEpochSecond(100L, 0, ZoneOffset.UTC));

	private YoutubeDownloadManager target;
	private YoutubeChannelDownloader channelDownloaderMock;
	private YoutubeVideoChunkMock outdatedChannelWithNewItemDownloadChunk;
	private FeedCache cache;

	@Before
	public void setUp() throws Exception
	{
		channelDownloaderMock = getYoutubeChannelDownloaderMock();
		final JsonFileSystemPersistence mock = mock(JsonFileSystemPersistence.class);
		when(mock.getChannel(anyString())).thenReturn(Optional.empty());
		cache = new FeedCache(mock);
		cache.update(CACHED_CHANNEL);
		cache.update(OUTDATED_CHANNEL_WITHOUT_NEW_ITEM);
		target = new YoutubeDownloadManager(channelDownloaderMock, cache);
	}

	@Test
	public void testGetChannelThatWasNotPreviouslyDownloaded() throws Exception
	{
		target.downloadFeed(UNCACHED_CHANNEL.getId());
		final Feed result = cache.get(UNCACHED_CHANNEL.getId()).get();
		assertThat(result).isEqualTo(UNCACHED_CHANNEL);
		verify(channelDownloaderMock).getVideoChunk(UNCACHED_CHANNEL.getId());
	}

	@Test
	public void shouldOnlyDownloadNewItemsFromYoutube() throws Exception
	{
		target.downloadFeed(OUTDATED_CHANNEL_WITH_NEW_ITEM.getId());
		final Feed result = cache.get(OUTDATED_CHANNEL_WITH_NEW_ITEM.getId()).get();
		assertThat(result).isEqualTo(OUTDATED_CHANNEL_WITH_NEW_ITEM);
		assertThat(outdatedChannelWithNewItemDownloadChunk.chunksLeft()).isEqualTo(1);
	}

	@Test
	public void shouldAppendNewVideosToChannelInCache() throws Exception
	{
		target.downloadFeed(OUTDATED_CHANNEL_WITH_NEW_ITEM.getId());
		final Feed result = cache.get(OUTDATED_CHANNEL_WITH_NEW_ITEM.getId()).get();
		assertThat(result).isEqualTo(OUTDATED_CHANNEL_WITH_NEW_ITEM);
	}

	private YoutubeChannelDownloader getYoutubeChannelDownloaderMock()
	{
		final YoutubeChannelDownloader channelDownloaderMock = mock(YoutubeChannelDownloader.class);

		outdatedChannelWithNewItemDownloadChunk = (YoutubeVideoChunkMock) createYoutubeVideoChunk(OUTDATED_CHANNEL_WITH_NEW_ITEM).get();
		mockWithResponse(channelDownloaderMock, OUTDATED_CHANNEL_WITH_NEW_ITEM.getId(), Optional.of(outdatedChannelWithNewItemDownloadChunk));
		mockWithResponse(channelDownloaderMock, CACHED_CHANNEL.getId(), createYoutubeVideoChunk(CACHED_CHANNEL));
		mockWithResponse(channelDownloaderMock, UNCACHED_CHANNEL.getId(), createYoutubeVideoChunk(UNCACHED_CHANNEL));
		mockWithResponse(channelDownloaderMock, NON_EXISTENT_CHANNEL_NAME, Optional.empty());
		return channelDownloaderMock;
	}

	private void mockWithResponse(YoutubeChannelDownloader channelDownloaderMock, String name, Optional<YoutubeVideoChunk> resp)
	{
		when(channelDownloaderMock.getVideoChunk(name)).thenReturn(resp);
	}

	/**
	 * Convert a channel into a bunch of chunks, each chunk will contain one video and the newest will be retrieved first
	 */
	private Optional<YoutubeVideoChunk> createYoutubeVideoChunk(final Feed channel)
	{
		return Optional.of(channel)
				.map(this::feedToPlaylistItem)
				.map(YoutubeVideoChunkMock::new);
	}

	private List<PlaylistItem> feedToPlaylistItem(Feed channel)
	{
		return Optional.of(channel)
				.map(Feed::getItems)
				.map(v -> v.stream()
						.map(this::toPlayListItem)
						.collect(Collectors.toList()))
				.get();
	}

	private PlaylistItem toPlayListItem(Item v)
	{
		return new PlaylistItem().setSnippet(new PlaylistItemSnippet()
				.setTitle(v.getTitle())
				.setDescription(v.getDescription())
				.setResourceId(new ResourceId().setVideoId(v.getId()))
				.setPublishedAt(new DateTime(v.getUploadDateAsLong(), 0)));
	}
}