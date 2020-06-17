package com.richo.reader.youtube_feed_service;

import com.richo.reader.youtube_feed_service.youtube.YoutubeChannelDownloader;
import com.richodemus.reader.dto.FeedId;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class PeriodicDownloadOrchestratorTest {
    private String saveRoot;
    private boolean running;

    @Before
    public void setUp() {
        running = true;
        saveRoot = "target/data/" + UUID.randomUUID();
        new File(saveRoot + "/feeds/richodemus").mkdirs();
    }

//    @Test(timeout = 10000)
//    public void shouldWorkMockedDownload() throws Exception {
//        final YoutubeDownloadManager downloaderMock = mock(YoutubeDownloadManager.class);
//        doAnswer(a ->
//        {
//            System.out.println("downloadFeed called!");
//            System.out.println(a.getArguments()[0]);
//            running = false;
//            return null;
//        }).when(downloaderMock).downloadFeed(any());
//        final FeedCache cache = new FeedCache(new JsonFileSystemPersistence(saveRoot));
//        cache.add(new FeedId("richodemus"));
//        final PeriodicDownloadOrchestrator target = new PeriodicDownloadOrchestrator(cache, downloaderMock, null, ZonedDateTime.now());
//
//        target.start();
//        System.out.println("Sleeping...");
//        while (running) {
//            Thread.sleep(10L);
//        }
//        System.out.println("Done");
//    }

    @Ignore("This test uses the live youtube api")
    @Test
    public void shouldWorkRealDownloader() throws Exception {
        final FeedCache cache = new FeedCache(new JsonFileSystemPersistence(saveRoot));
        final YoutubeDownloadManager downloader = new YoutubeDownloadManager(new YoutubeChannelDownloader(null, "api-key-here"), cache);
        final PeriodicDownloadOrchestrator target = new PeriodicDownloadOrchestrator(cache, downloader, null, ZonedDateTime.now());
        target.start();
        System.out.println("Sleeping...");
        while (!new File(saveRoot + "/feeds/richodemus/data.json").exists()) {
            Thread.sleep(10L);
        }
        System.out.println("Done");
    }

    @Ignore("This test uses the live youtube api")
    @Test
    public void actuallyDownloadEverything() throws Exception {
        final FeedCache cache = new FeedCache(new JsonFileSystemPersistence("../data"));
        final YoutubeDownloadManager downloader = new YoutubeDownloadManager(new YoutubeChannelDownloader(null, "api-key-here"), cache);
        final PeriodicDownloadOrchestrator target = new PeriodicDownloadOrchestrator(cache, downloader, null, ZonedDateTime.now());
        target.start();
        System.out.println("Sleeping...");
        Thread.sleep(Duration.of(5, MINUTES).toMillis());
    }
}