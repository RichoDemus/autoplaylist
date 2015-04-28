package com.richo.reader.backend.youtube;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.ResourceId;
import com.richo.reader.backend.youtube.download.YouTubeVideoChuck;
import com.richo.reader.backend.youtube.download.YoutubeChannelDownloader;
import com.richo.reader.backend.youtube.model.YoutubeChannel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

public class YoutubeChannelServiceTest
{
	private static final String CHANNEL_NAME = "channel_name";
	private URL url;

	@Before
	public void setUp() throws Exception
	{
		url = new URL("https://www.youtube.com/watch?v=_0S1jebDBzk");
	}

	@Test
	public void testGetChannelThatWasNotPreviouslyDownloaded() throws Exception
	{
		final YoutubeChannelDownloader channelDownloaderMock = Mockito.mock(YoutubeChannelDownloader.class);
		final PlaylistItem playlistItem = new PlaylistItem().setSnippet(new PlaylistItemSnippet()
				.setTitle("title1")
				.setDescription("description1")
				.setResourceId(new ResourceId().setVideoId("_0S1jebDBzk"))
				.setPublishedAt(new DateTime(1409920676000L, 0)));
		final Queue<List<PlaylistItem>> items = new LinkedList<>();
		items.add(Collections.singletonList(playlistItem));
		final YouTubeVideoChuck videoChunkMock = new YoutubeVideoChunkMock(items);
		Mockito.when(channelDownloaderMock.getVideoChunk(CHANNEL_NAME)).thenReturn(Optional.of(videoChunkMock));

		final YoutubeChannelService target = new YoutubeChannelService(channelDownloaderMock);
		final YoutubeChannel result = target.getChannelByName(CHANNEL_NAME).get();

		Assert.assertNotNull(result);
		Assert.assertEquals(CHANNEL_NAME, result.getName());

		result.getVideos().forEach((video) ->
		{
			Assert.assertEquals("title1", video.getTitle());
			Assert.assertEquals("description1", video.getDescription());
			Assert.assertEquals(url, video.getUrl());
			Assert.assertEquals(LocalDateTime.of(2014, 9, 5, 12, 37, 56), video.getUploadDate());

		});
	}
}