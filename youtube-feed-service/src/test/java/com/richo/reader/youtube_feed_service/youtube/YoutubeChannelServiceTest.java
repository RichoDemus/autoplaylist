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
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class YoutubeChannelServiceTest
{
	private static final String NON_EXISTENT_CHANNEL_NAME = "non existerino";
	private static final Item CACHED_CHANNEL_FIRST_VIDEO = new Item("cached_1", "description1", "_0S1jebDBzk", LocalDateTime.of(2014, 9, 5, 12, 37, 56));
	private static final Feed CACHED_CHANNEL = new Feed(
			"cached_channel",
			singletonList(CACHED_CHANNEL_FIRST_VIDEO),
			LocalDateTime.now());

	private static final Item UNCACHED_CHANNEL_FIRST_VIDEO = new Item("uncached_1", "description1", "_0S1jebDBzk", LocalDateTime.of(2014, 9, 5, 12, 37, 56));
	private static final Feed UNCACHED_CHANNEL = new Feed(
			"uncached_channel",
			singletonList(UNCACHED_CHANNEL_FIRST_VIDEO),
			LocalDateTime.now());

	private static final Item OUTDATED_CHANNEL_FIRST_VIDEO = new Item("outdated_1", "description1", "_0S1jebDBzk", LocalDateTime.of(2014, 9, 5, 12, 37, 56));
	private static final Item OUTDATED_CHANNEL_SECOND_VIDEO = new Item("outdated_2", "description2", "_0S1jebdDBzk", LocalDateTime.of(2014, 9, 10, 12, 37, 56));
	private static final Item OUTDATED_CHANNEL_NOT_CACHED_VIDEO = new Item("outdated_noncached_3", "description3", "_0s1jebDBze", LocalDateTime.of(2014, 11, 5, 12, 37, 56));
	private static final Feed OUTDATED_CHANNEL_WITH_NEW_ITEM = new Feed(
			"outdated_channel",
			asList(OUTDATED_CHANNEL_FIRST_VIDEO, OUTDATED_CHANNEL_SECOND_VIDEO, OUTDATED_CHANNEL_NOT_CACHED_VIDEO),
			LocalDateTime.ofEpochSecond(100L, 0, ZoneOffset.UTC));
	private static final Feed OUTDATED_CHANNEL_WITHOUT_NEW_ITEM = new Feed(
			"outdated_channel",
			asList(OUTDATED_CHANNEL_FIRST_VIDEO, OUTDATED_CHANNEL_SECOND_VIDEO),
			LocalDateTime.ofEpochSecond(100L, 0, ZoneOffset.UTC));

	private YoutubeChannelService target;
	private YoutubeChannelDownloader channelDownloaderMock;
	private YoutubeVideoChunkMock outdatedChannelWithNewItemDownloadChunk;
	private FeedCache cache;

	@Before
	public void setUp() throws Exception
	{
		channelDownloaderMock = getYoutubeChannelDownloaderMock();
		cache = new FeedCache(new JsonFileSystemPersistence("target/data"));
		cache.update(CACHED_CHANNEL);
		cache.update(OUTDATED_CHANNEL_WITHOUT_NEW_ITEM);
		target = new YoutubeChannelService(channelDownloaderMock, cache, Duration.of(1, ChronoUnit.HOURS));
	}

	@Test
	public void testGetChannelThatWasNotPreviouslyDownloaded() throws Exception
	{
		target.downloadFeed(UNCACHED_CHANNEL.getId());
		final Feed result = cache.get(UNCACHED_CHANNEL.getId()).get();
		Assertions.assertThat(result).isEqualTo(UNCACHED_CHANNEL);
		Mockito.verify(channelDownloaderMock).getVideoChunk(UNCACHED_CHANNEL.getId());
	}

/*	@Test
	public void testShouldNotFetchChannelIfAlreadyCachedAndRefreshIntervalNotPassed() throws Exception
	{
		final Feed result = target.getChannelByName(CACHED_CHANNEL.getName()).get();
		Assertions.assertThat(result).isEqualTo(CACHED_CHANNEL);
		Mockito.verifyZeroInteractions(channelDownloaderMock);
	}

	@Test
	public void testShouldFetchChannelIfRefreshIntervalHasPassed() throws Exception
	{
		final Feed result = target.getChannelByName(OUTDATED_CHANNEL_WITH_NEW_ITEM.getName()).get();
		Assertions.assertThat(result).isEqualTo(OUTDATED_CHANNEL_WITH_NEW_ITEM);
		Mockito.verify(channelDownloaderMock).getVideoChunk(OUTDATED_CHANNEL_WITH_NEW_ITEM.getName());
	}

	@Test
	public void shouldNotFetchAllChannelsIfRrefreshIntervalHasPassed() throws Exception
	{
		final Feed result = target.getChannelByName(OUTDATED_CHANNEL_WITH_NEW_ITEM.getName()).get();
		Assertions.assertThat(result).isEqualTo(OUTDATED_CHANNEL_WITH_NEW_ITEM);
		assertThat(outdatedChannelWithNewItemDownloadChunk.chunksLeft()).isEqualTo(1);
	}

	@Test
	public void shouldAppendNewVideosToChannelInCache() throws Exception
	{
		target.getChannelByName(OUTDATED_CHANNEL_WITH_NEW_ITEM.getName()).get();
		final Feed result = cache.getChannel(OUTDATED_CHANNEL_WITH_NEW_ITEM.getName()).get();
		Assertions.assertThat(result).isEqualTo(OUTDATED_CHANNEL_WITH_NEW_ITEM);
	}

	@Test
	public void shouldReturnEmptyOptionalIfChannelDoesntExist() throws Exception
	{
		final Optional<Feed> result = target.getChannelByName(NON_EXISTENT_CHANNEL_NAME);
		Assertions.assertThat(result.isPresent()).isFalse();
	}*/

	private YoutubeChannelDownloader getYoutubeChannelDownloaderMock()
	{
		final YoutubeChannelDownloader channelDownloaderMock = Mockito.mock(YoutubeChannelDownloader.class);

		outdatedChannelWithNewItemDownloadChunk = (YoutubeVideoChunkMock) createYoutubeVideoChunk(OUTDATED_CHANNEL_WITH_NEW_ITEM).get();
		mockWithResponse(channelDownloaderMock, OUTDATED_CHANNEL_WITH_NEW_ITEM.getId(), Optional.of(outdatedChannelWithNewItemDownloadChunk));
		mockWithResponse(channelDownloaderMock, CACHED_CHANNEL.getId(), createYoutubeVideoChunk(CACHED_CHANNEL));
		mockWithResponse(channelDownloaderMock, UNCACHED_CHANNEL.getId(), createYoutubeVideoChunk(UNCACHED_CHANNEL));
		mockWithResponse(channelDownloaderMock, NON_EXISTENT_CHANNEL_NAME, Optional.empty());
		return channelDownloaderMock;
	}

	private void mockWithResponse(YoutubeChannelDownloader channelDownloaderMock, String name, Optional<YoutubeVideoChunk> resp)
	{
		Mockito.when(channelDownloaderMock.getVideoChunk(name)).thenReturn(resp);
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