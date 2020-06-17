package com.richo.reader.youtube_feed_service

import com.google.common.util.concurrent.ThreadFactoryBuilder
import org.slf4j.LoggerFactory
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeriodicDownloadOrchestrator internal constructor(
        private val cache: FeedCache,
        private val downloader: YoutubeDownloadManager,
        private val feedService: YoutubeFeedService,
        timeToRun: ZonedDateTime
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val executor: ScheduledExecutorService
    private val timeToRun: ZonedDateTime
    var isRunning = false
        private set
    private var lastRun = LocalDateTime.MIN
    private var lastRunOutCome: String? = "not run"

    @Inject
    constructor(cache: FeedCache, downloader: YoutubeDownloadManager, feedService: YoutubeFeedService) : this(cache, downloader, feedService, midnight) {
    }

    private fun onException(thread: Thread, throwable: Throwable) {
        logger.error("Thread {} threw uncaught exception:", thread, throwable)
    }

    // called from some dropwizard thing in web
    fun start() {
        val millisecondsUntilMidnight = calculateDelayUntilMidnight()
        executor.scheduleAtFixedRate({ downloadStuff() }, millisecondsUntilMidnight, MILLISECONDS_IN_A_DAY, TimeUnit.MILLISECONDS)
        logger.info("Started orchestrator, will run at {}", Instant.ofEpochMilli(System.currentTimeMillis() + millisecondsUntilMidnight).toString())
    }

    private fun downloadStuff() {
        try {
            logger.info("Midnight, time to download")
            isRunning = true
            feedService.updateChannelsAndVideos()
            lastRun = LocalDateTime.now()
            lastRunOutCome = "OK"
        } catch (e: Exception) {
            logger.error("Failed to download feeds", e)
            lastRunOutCome = e.message
        } finally {
            isRunning = false
        }
    }

    fun downloadEverythingOnce() {
        executor.execute { downloadStuff() }
    }

    fun stop() {
        executor.shutdown()
    }

    fun lastRun(): LocalDateTime {
        return lastRun
    }

    fun lastRunOutCome(): String? {
        return lastRunOutCome
    }

    private fun calculateDelayUntilMidnight(): Long {
        return ChronoUnit.MILLIS.between(Instant.now(), timeToRun)
    }

    companion object {
        private val MILLISECONDS_IN_A_DAY = Duration.of(1, ChronoUnit.DAYS).toMillis()
        private val midnight: ZonedDateTime
            private get() = ZonedDateTime.now(ZoneOffset.UTC)
                    .toLocalDate()
                    .plusDays(1)
                    .atStartOfDay(ZoneOffset.UTC)
    }

    init {
        executor = ScheduledThreadPoolExecutor(1, ThreadFactoryBuilder().setNameFormat("yt-downloader-%s").setUncaughtExceptionHandler { thread: Thread, throwable: Throwable -> onException(thread, throwable) }.build())
        this.timeToRun = timeToRun
    }
}