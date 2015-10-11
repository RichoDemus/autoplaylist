package com.richo.reader.backend.youtube;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.ResourceId;
import com.richo.reader.backend.persistence.InMemoryPersistence;
import com.richo.reader.backend.persistence.YoutubeChannelPersistence;
import com.richo.reader.backend.youtube.download.YoutubeChannelDownloader;
import com.richo.reader.backend.youtube.download.YoutubeVideoChunk;
import com.richo.reader.backend.youtube.model.YoutubeChannel;
import com.richo.reader.backend.youtube.model.YoutubeVideo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class YoutubeChannelServiceTest
{
	public static final YoutubeVideo CACHED_CHANNEL_FIRST_VIDEO = new YoutubeVideo("title1", "description1", "_0S1jebDBzk", LocalDateTime.of(2014, 9, 5, 12, 37, 56));
	public static final YoutubeChannel CACHED_CHANNEL = new YoutubeChannel(
			"cached_channel",
			new HashSet<>(singletonList(CACHED_CHANNEL_FIRST_VIDEO)));

	public static final YoutubeVideo UNCACHED_CHANNEL_FIRST_VIDEO = new YoutubeVideo("title1", "description1", "_0S1jebDBzk", LocalDateTime.of(2014, 9, 5, 12, 37, 56));
	public static final YoutubeChannel UNCACHED_CHANNEL = new YoutubeChannel(
			"uncached_channel",
			new HashSet<>(singletonList(UNCACHED_CHANNEL_FIRST_VIDEO)));

	public static final YoutubeVideo OUTDATED_CHANNEL_FIRST_VIDEO = new YoutubeVideo("title1", "description1", "_0S1jebDBzk", LocalDateTime.of(2014, 9, 5, 12, 37, 56));
	public static final YoutubeVideo OUTDATED_CHANNEL_SECOND_VIDEO = new YoutubeVideo("title2", "description2", "_0S1jebdDBzk", LocalDateTime.of(2014, 9, 10, 12, 37, 56));
	public static final YoutubeVideo OUTDATED_CHANNEL_NOT_CACHED_VIDEO = new YoutubeVideo("title3", "description3", "_0s1jebDBze", LocalDateTime.of(2014, 11, 5, 12, 37, 56));
	public static final YoutubeChannel OUTDATED_CHANNEL_WITH_NEW_ITEM = new YoutubeChannel(
			"outdated_channel",
			new HashSet<>(asList(OUTDATED_CHANNEL_FIRST_VIDEO, OUTDATED_CHANNEL_SECOND_VIDEO, OUTDATED_CHANNEL_NOT_CACHED_VIDEO)), Instant.ofEpochSecond(100));
	public static final YoutubeChannel OUTDATED_CHANNEL_WITHOUT_NEW_ITEM = new YoutubeChannel(
			"outdated_channel",
			new HashSet<>(asList(OUTDATED_CHANNEL_FIRST_VIDEO, OUTDATED_CHANNEL_SECOND_VIDEO)), Instant.ofEpochSecond(100));

	private YoutubeChannelService target;
	private YoutubeChannelDownloader channelDownloaderMock;
	private YoutubeVideoChunkMock outdatedChannelWithNewItemDownloadChunk;
	private YoutubeChannelPersistence cache;

	@Before
	public void setUp() throws Exception
	{
		channelDownloaderMock = getYoutubeChannelDownloaderMock();
		cache = new YoutubeChannelPersistence(new InMemoryPersistence(), new InMemoryPersistence());
		cache.updateChannel(CACHED_CHANNEL);
		cache.updateChannel(OUTDATED_CHANNEL_WITHOUT_NEW_ITEM);
		target = new YoutubeChannelService(channelDownloaderMock, cache, Duration.of(1, ChronoUnit.HOURS));
	}

	@Test
	public void testGetChannelThatWasNotPreviouslyDownloaded() throws Exception
	{
		final YoutubeChannel result = target.getChannelByName(UNCACHED_CHANNEL.getName()).get();
		assertThat(result).isEqualTo(UNCACHED_CHANNEL);
		verify(channelDownloaderMock).getVideoChunk(UNCACHED_CHANNEL.getName());
	}

	@Test
	public void testShouldNotFetchChannelIfAlreadyCachedAndRefreshIntervalNotPassed() throws Exception
	{
		final YoutubeChannel result = target.getChannelByName(CACHED_CHANNEL.getName()).get();
		assertThat(result).isEqualTo(CACHED_CHANNEL);
		verifyZeroInteractions(channelDownloaderMock);
	}

	@Test
	public void testShouldFetchChannelIfRefreshIntervalHasPassed() throws Exception
	{
		final YoutubeChannel result = target.getChannelByName(OUTDATED_CHANNEL_WITH_NEW_ITEM.getName()).get();
		assertThat(result).isEqualTo(OUTDATED_CHANNEL_WITH_NEW_ITEM);
		verify(channelDownloaderMock).getVideoChunk(OUTDATED_CHANNEL_WITH_NEW_ITEM.getName());
	}

	@Test
	public void shouldNotFetchAllChannelsIfRrefreshIntervalHasPassed() throws Exception
	{
		final YoutubeChannel result = target.getChannelByName(OUTDATED_CHANNEL_WITH_NEW_ITEM.getName()).get();

		assertThat(result).isEqualTo(OUTDATED_CHANNEL_WITH_NEW_ITEM);
		assertThat(outdatedChannelWithNewItemDownloadChunk.chunksLeft()).isEqualTo(1);
	}

	@Test
	public void shouldAppendNewVideosToChannelInCache() throws Exception
	{
		target.getChannelByName(OUTDATED_CHANNEL_WITH_NEW_ITEM.getName()).get();

		final YoutubeChannel result = cache.getChannel(OUTDATED_CHANNEL_WITH_NEW_ITEM.getName()).get();

		assertThat(result).isEqualTo(OUTDATED_CHANNEL_WITH_NEW_ITEM);

	}

	private YoutubeChannelDownloader getYoutubeChannelDownloaderMock()
	{
		final YoutubeChannelDownloader channelDownloaderMock = mock(YoutubeChannelDownloader.class);

		Mockito.when(channelDownloaderMock.getVideoChunk(CACHED_CHANNEL.getName()))
				.thenReturn(createYoutubeVideoChunk(CACHED_CHANNEL));

		Mockito.when(channelDownloaderMock.getVideoChunk(UNCACHED_CHANNEL.getName()))
				.thenReturn(createYoutubeVideoChunk(UNCACHED_CHANNEL));
		outdatedChannelWithNewItemDownloadChunk = (YoutubeVideoChunkMock) createYoutubeVideoChunk(OUTDATED_CHANNEL_WITH_NEW_ITEM).get();
		Mockito.when(channelDownloaderMock.getVideoChunk(OUTDATED_CHANNEL_WITH_NEW_ITEM.getName()))
				.thenReturn(Optional.of(outdatedChannelWithNewItemDownloadChunk));

		return channelDownloaderMock;
	}

	private Optional<YoutubeVideoChunk> createYoutubeVideoChunk(final YoutubeChannel channel)
	{
		return Optional.of(channel)
				.map(this::channelToPlayListItem)
				.map(YoutubeVideoChunkMock::new);
	}

	private List<PlaylistItem> channelToPlayListItem(YoutubeChannel channel)
	{
		return Optional.of(channel)
				.map(YoutubeChannel::getVideos)
				.map(v -> v.stream()
						.map(this::toPlayListItem)
						.collect(Collectors.toList()))
				.get();
	}

	private PlaylistItem toPlayListItem(YoutubeVideo v)
	{
		return new PlaylistItem().setSnippet(new PlaylistItemSnippet()
				.setTitle(v.getTitle())
				.setDescription(v.getDescription())
				.setResourceId(new ResourceId().setVideoId(v.getVideoId()))
				.setPublishedAt(new DateTime(v.getUploadDateAsLong(), 0)));
	}
}