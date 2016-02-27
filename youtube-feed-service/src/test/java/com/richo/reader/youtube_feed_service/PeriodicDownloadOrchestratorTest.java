package com.richo.reader.youtube_feed_service;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class PeriodicDownloadOrchestratorTest
{
	public String saveRoot;
	public boolean running = true;

	@Before
	public void setUp() throws Exception
	{
		saveRoot = "target/data/" + UUID.randomUUID();
		new File(saveRoot + "/feeds/a_feed").mkdirs();
	}

	@Test(timeout = 10000)
	public void shouldWorkMockedDownload() throws Exception
	{
		final YoutubeDownloadManager downloaderMock = mock(YoutubeDownloadManager.class);
		doAnswer(a ->
		{
			System.out.println("downloadFeed called!");
			System.out.println(a.getArguments()[0]);
			running = false;
			return null;
		}).when(downloaderMock).downloadFeed(any());
		final PeriodicDownloadOrchestrator target = new PeriodicDownloadOrchestrator(new FeedCache(new JsonFileSystemPersistence(saveRoot)), downloaderMock, ZonedDateTime.now());
		target.start();
		System.out.println("Sleeping...");
		while (running)
		{
			Thread.sleep(10L);
		}
		System.out.println("Done");
	}
}