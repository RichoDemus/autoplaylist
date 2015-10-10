package com.richo.reader.backend.youtube;


import com.google.api.services.youtube.model.PlaylistItem;
import com.richo.reader.backend.youtube.download.YouTubeVideoChuck;
import com.richo.reader.backend.youtube.download.YoutubeChannelDownloader;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class YoutubeChannelDownloaderTest
{
	@Ignore("This test uses the live youtube api")
	@Test
	public void testDownloadVideosFromSingleVideoChannel() throws Exception
	{

		final YoutubeChannelDownloader downloader = new YoutubeChannelDownloader("AIzaSyChI7lMyLfc1ckOqcC-z2Oz-Lrq6d09x30");

		final YouTubeVideoChuck chunk = downloader.getVideoChunk("RichoDemus").get();

		List<PlaylistItem> videoChunk = chunk.getNextVideoChunk();

		Assert.assertEquals(videoChunk.size(), 1);
		Assert.assertEquals("T-Rex optical illusion", videoChunk.get(0).getSnippet().getTitle());
		Assert.assertEquals("Zs6bAFlcH0M", videoChunk.get(0).getSnippet().getResourceId().getVideoId());
		Assert.assertEquals(1409920676000L, videoChunk.get(0).getSnippet().getPublishedAt().getValue());
		Assert.assertEquals(0, videoChunk.get(0).getSnippet().getPublishedAt().getTimeZoneShift());

	}

	@Ignore("This test uses the live youtube api")
	@Test
	public void testDownloadVideosFromChannelWithLotsOfVideos() throws Exception
	{
		final YoutubeChannelDownloader downloader = new YoutubeChannelDownloader("AIzaSyChI7lMyLfc1ckOqcC-z2Oz-Lrq6d09x30");

		final YouTubeVideoChuck chunk = downloader.getVideoChunk("Thunderf00t").get();

		List<PlaylistItem> videos = chunk.getNextVideoChunk();

		Assert.assertEquals(videos.size(), 50);
		videos.stream().map((video) -> video.getSnippet().getTitle()).collect(Collectors.toList()).forEach(System.out::println);

		videos = chunk.getNextVideoChunk();

		Assert.assertEquals(videos.size(), 50);
		videos.stream().map((video) -> video.getSnippet().getTitle()).collect(Collectors.toList()).forEach(System.out::println);


	}
}