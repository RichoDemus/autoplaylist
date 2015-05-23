package com.richo.reader.backend.youtube;

import com.google.api.client.util.DateTime;
import com.google.api.client.util.Sets;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.ResourceId;
import com.richo.reader.backend.persistence.InMemoryPersistence;
import com.richo.reader.backend.persistence.YoutubeChannelPersistence;
import com.richo.reader.backend.youtube.download.YouTubeVideoChuck;
import com.richo.reader.backend.youtube.download.YoutubeChannelDownloader;
import com.richo.reader.backend.youtube.model.YoutubeChannel;
import com.richo.reader.backend.youtube.model.YoutubeVideo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

public class YoutubeChannelServiceTest
{
	private static final String CHANNEL_NAME = "channel_name";
	private static final String VIDEO_ID = "_0S1jebDBzk";

	private URL url;

	@Before
	public void setUp() throws Exception
	{
		url = new URL("https://www.youtube.com/watch?v=" + VIDEO_ID);
	}

	@Test
	public void testGetChannelThatWasNotPreviouslyDownloaded() throws Exception
	{
		final YoutubeChannelDownloader channelDownloaderMock = getYoutubeChannelDownloaderMock();

		final YoutubeChannelPersistence cacheMock = Mockito.mock(YoutubeChannelPersistence.class);
		Mockito.when(cacheMock.getChannel(Matchers.anyString())).thenReturn(Optional.empty());

		final YoutubeChannelService target = new YoutubeChannelService(channelDownloaderMock, cacheMock, Duration.of(1, ChronoUnit.HOURS));
		final YoutubeChannel result = target.getChannelByName(CHANNEL_NAME).get();

		Assert.assertNotNull(result);
		Assert.assertEquals(CHANNEL_NAME, result.getName());

		Assert.assertEquals(1, result.getVideos().size());
		result.getVideos().forEach((video) ->
		{
			Assert.assertEquals("title1", video.getTitle());
			Assert.assertEquals("description1", video.getDescription());
			Assert.assertEquals(url, video.getUrl());
			Assert.assertEquals(LocalDateTime.of(2014, 9, 5, 12, 37, 56), video.getUploadDate());

		});
	}

	private YoutubeChannelDownloader getYoutubeChannelDownloaderMock()
	{
		final YoutubeChannelDownloader channelDownloaderMock = Mockito.mock(YoutubeChannelDownloader.class);
		final PlaylistItem playlistItem = new PlaylistItem().setSnippet(new PlaylistItemSnippet()
				.setTitle("title1")
				.setDescription("description1")
				.setResourceId(new ResourceId().setVideoId(VIDEO_ID))
				.setPublishedAt(new DateTime(1409920676000L, 0)));
		final Queue<List<PlaylistItem>> items = new LinkedList<>();
		items.add(Collections.singletonList(playlistItem));
		final YouTubeVideoChuck videoChunkMock = new YoutubeVideoChunkMock(items);
		Mockito.when(channelDownloaderMock.getVideoChunk(CHANNEL_NAME)).thenReturn(Optional.of(videoChunkMock));
		return channelDownloaderMock;
	}

	@Test
	public void testShouldNotFetchChannelIfAlreadyCachedAndRefreshIntervallNotPassed() throws Exception
	{
		final YoutubeChannelPersistence cache = new YoutubeChannelPersistence(new InMemoryPersistence(), new InMemoryPersistence());
		final Set<YoutubeVideo> videos = Sets.newTreeSet();
		videos.add(new YoutubeVideo("title1", "description1", VIDEO_ID, LocalDateTime.of(2014, 9, 5, 12, 37, 56)));
		final YoutubeChannel channel = new YoutubeChannel(CHANNEL_NAME, videos);
		cache.updateChannel(channel);

		final YoutubeChannelDownloader mockThatShouldNotBeCalled = Mockito.mock(YoutubeChannelDownloader.class);
		Mockito.verifyZeroInteractions(mockThatShouldNotBeCalled);

		final YoutubeChannelService target = new YoutubeChannelService(mockThatShouldNotBeCalled, cache, Duration.of(1, ChronoUnit.HOURS));

		final YoutubeChannel result = target.getChannelByName(CHANNEL_NAME).get();

		Assert.assertEquals(1, result.getVideos().size());
		result.getVideos().forEach((video) ->
		{
			Assert.assertEquals("title1", video.getTitle());
			Assert.assertEquals("description1", video.getDescription());
			Assert.assertEquals(url, video.getUrl());
			Assert.assertEquals(LocalDateTime.of(2014, 9, 5, 12, 37, 56), video.getUploadDate());

		});
	}

	@Test
	public void testShouldFetchChannelIfRefreshIntervallHasPassed() throws Exception
	{
		final YoutubeChannelPersistence cache = new YoutubeChannelPersistence(new InMemoryPersistence(), new InMemoryPersistence());
		final Set<YoutubeVideo> videos = Sets.newTreeSet();
		videos.add(new YoutubeVideo("title1", "description_to_be_replaced", VIDEO_ID, LocalDateTime.of(2014, 9, 5, 12, 37, 56)));
		final YoutubeChannel channel = new YoutubeChannel(CHANNEL_NAME, videos, Instant.ofEpochSecond(100));
		cache.updateChannel(channel);

		final YoutubeChannelDownloader channelDownloaderMock = getYoutubeChannelDownloaderMock();

		final YoutubeChannelService target = new YoutubeChannelService(channelDownloaderMock, cache, Duration.of(1, ChronoUnit.HOURS));

		final YoutubeChannel result = target.getChannelByName(CHANNEL_NAME).get();

		Assert.assertEquals(1, result.getVideos().size());
		result.getVideos().forEach((video) ->
		{
			Assert.assertEquals("title1", video.getTitle());
			Assert.assertEquals("description1", video.getDescription());
			Assert.assertEquals(url, video.getUrl());
			Assert.assertEquals(LocalDateTime.of(2014, 9, 5, 12, 37, 56), video.getUploadDate());
		});

		Assert.assertEquals(1, cache.getChannel(CHANNEL_NAME).get().getVideos().size());
		cache.getChannel(CHANNEL_NAME).get().getVideos().forEach((video) ->
		{
			Assert.assertEquals("title1", video.getTitle());
			Assert.assertEquals("description1", video.getDescription());
			Assert.assertEquals(url, video.getUrl());
			Assert.assertEquals(LocalDateTime.of(2014, 9, 5, 12, 37, 56), video.getUploadDate());
		});

	}
}