package com.richo.reader.youtube_feed_service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PeriodicDownloadOrchestrator
{
	private static final long MILLISECONDS_IN_A_DAY = 1000 * 60 * 60 * 24;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final FeedCache cache;
	private final YoutubeDownloadManager downloader;
	private final ScheduledExecutorService executor;
	final private ZonedDateTime timeToRun;

	@Inject
	public PeriodicDownloadOrchestrator(FeedCache cache, YoutubeDownloadManager downloader)
	{
		this(cache, downloader, getMidnight());
	}

	private static ZonedDateTime getMidnight()
	{
		return ZonedDateTime.now(ZoneOffset.UTC)
				.toLocalDate()
				.plusDays(1)
				.atStartOfDay(ZoneOffset.UTC);
	}

	PeriodicDownloadOrchestrator(FeedCache cache, YoutubeDownloadManager downloader, ZonedDateTime timeToRun)
	{
		this.cache = cache;
		this.downloader = downloader;
		this.executor = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder().setNameFormat("yt-downloader-%s").build());
		this.timeToRun = timeToRun;
	}

	public void start()
	{
		final long milisecondsUntilMidnight = calculateDelayUntilMidnight();
		executor.scheduleAtFixedRate(this::addDownloadsTasksToExecutor, milisecondsUntilMidnight, MILLISECONDS_IN_A_DAY, TimeUnit.MILLISECONDS);
		logger.info("Started orchestrator, will run at {}", Instant.ofEpochMilli(System.currentTimeMillis() + milisecondsUntilMidnight).toString());
	}

	public void downloadEverythingOnce()
	{
		executor.execute(this::addDownloadsTasksToExecutor);
	}

	public void stop()
	{
		executor.shutdown();
	}

	private void addDownloadsTasksToExecutor()
	{
		logger.info("Midnight, time to download");
		final List<String> feedIds = cache.getAllFeedIds();
		Collections.sort(feedIds);
		logger.info("{} feeds to download", feedIds.size());

		feedIds.forEach(downloader::downloadFeed);
	}

	private long calculateDelayUntilMidnight()
	{
		return ChronoUnit.MILLIS.between(Instant.now(), timeToRun);
	}
}
