package com.richo.reader.youtube_feed_service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PeriodicDownloadOrchestrator
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final FeedCache cache;
	private final YoutubeDownloadManager downloader;
	private final ScheduledExecutorService executor;

	@Inject
	public PeriodicDownloadOrchestrator(FeedCache cache, YoutubeDownloadManager downloader)
	{
		this.cache = cache;
		this.downloader = downloader;
		executor = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder().setNameFormat("yt-downloader-%s").build());
	}

	public void start()
	{
		final long milisecondsUntilMidnight = calculateDelayUntilMidnight();
		executor.scheduleAtFixedRate(this::addDownloadsTasksToExecutor, milisecondsUntilMidnight, 1, TimeUnit.DAYS);
	}

	public void stop()
	{
		executor.shutdown();
	}

	private void addDownloadsTasksToExecutor()
	{
		final List<String> feedIds = cache.getAllFeedIds();
		logger.info("Midnight, time to download {} feeds", feedIds.size());

		feedIds.forEach(downloader::downloadFeed);
	}

	private long calculateDelayUntilMidnight()
	{
		ZoneId zoneId = ZoneOffset.UTC;
		ZonedDateTime now = ZonedDateTime.now( zoneId );
		LocalDate tomorrow = now.toLocalDate().plusDays(1);
		ZonedDateTime tomorrowStart = tomorrow.atStartOfDay( zoneId );
		return ChronoUnit.MILLIS.between(Instant.now(), tomorrowStart);
	}
}
