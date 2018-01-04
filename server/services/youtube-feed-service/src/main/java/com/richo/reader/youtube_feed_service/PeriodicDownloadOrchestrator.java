package com.richo.reader.youtube_feed_service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.richodemus.reader.dto.FeedId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.time.temporal.ChronoUnit.HOURS;

@Singleton
public class PeriodicDownloadOrchestrator {
    private static final long MILLISECONDS_IN_A_DAY = 1000 * 60 * 60 * 24;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final FeedCache cache;
    private final YoutubeDownloadManager downloader;
    private final ScheduledExecutorService executor;
    private final ZonedDateTime timeToRun;
    private boolean isRunning = false;
    private LocalDateTime lastRun = LocalDateTime.MIN;

    @Inject
    public PeriodicDownloadOrchestrator(FeedCache cache, YoutubeDownloadManager downloader) {
        this(cache, downloader, getMidnight());
    }

    private static ZonedDateTime getMidnight() {
        return ZonedDateTime.now(ZoneOffset.UTC)
                .toLocalDate()
                .plusDays(1)
                .atStartOfDay(ZoneOffset.UTC);
    }

    PeriodicDownloadOrchestrator(FeedCache cache, YoutubeDownloadManager downloader, ZonedDateTime timeToRun) {
        this.cache = cache;
        this.downloader = downloader;
        this.executor = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder().setNameFormat("yt-downloader-%s").setUncaughtExceptionHandler(this::onException).build());
        this.timeToRun = timeToRun;
    }

    private void onException(Thread thread, Throwable throwable) {
        logger.error("Thread {} threw uncaught exception:", thread, throwable);
    }

    public void start() {
        final long millisecondsUntilMidnight = calculateDelayUntilMidnight();
        final long fourInTheMorning = millisecondsUntilMidnight + Duration.of(4, HOURS).toMillis();
        executor.scheduleAtFixedRate(this::addDownloadsTasksToExecutor, millisecondsUntilMidnight, MILLISECONDS_IN_A_DAY, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(this::addUpdateStatisticsTasksToExecutor, fourInTheMorning, MILLISECONDS_IN_A_DAY, TimeUnit.MILLISECONDS);
        logger.info("Started orchestrator, will run at {}", Instant.ofEpochMilli(System.currentTimeMillis() + millisecondsUntilMidnight).toString());
    }

    public void downloadEverythingOnce() {
        executor.execute(this::addDownloadsTasksToExecutor);
    }

    public void updateEverythingOnce() {
        executor.execute(this::addUpdateStatisticsTasksToExecutor);
    }

    public void stop() {
        executor.shutdown();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public LocalDateTime lastRun() {
        return lastRun;
    }

    private void addDownloadsTasksToExecutor() {
        isRunning = true;
        try {
            logger.info("Midnight, time to download");
            // todo get all feedIds from event store instead of disk
            final List<FeedId> feedIds = cache.getAllFeedIds();
            feedIds.sort(Comparator.comparing(FeedId::getValue));
            logger.info("{} feeds to download", feedIds.size());

            feedIds.forEach(feedId -> runWithExceptionHandling(feedId, downloader::downloadFeed));
            lastRun = LocalDateTime.now();
        } finally {
            isRunning = false;
        }
    }

    private void addUpdateStatisticsTasksToExecutor() {
        isRunning = true;
        try {
            logger.info("It's 4 in the morning, time to update statistics");
            cache.getAllFeedIds().forEach(feedId -> runWithExceptionHandling(feedId, downloader::updateFeedStatistics));
            lastRun = LocalDateTime.now();
        } finally {
            isRunning = false;
        }
    }

    private long calculateDelayUntilMidnight() {
        return ChronoUnit.MILLIS.between(Instant.now(), timeToRun);
    }

    private void runWithExceptionHandling(FeedId feedId, Consumer<FeedId> consumer) {
        try {
            consumer.accept(feedId);
        } catch (Exception e) {
            logger.error("Failed to run job", e);
            throw e;
        }
    }
}
