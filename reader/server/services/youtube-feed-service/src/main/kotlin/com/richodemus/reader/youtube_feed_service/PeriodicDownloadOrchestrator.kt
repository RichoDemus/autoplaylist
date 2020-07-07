package com.richodemus.reader.youtube_feed_service

import com.google.common.util.concurrent.ThreadFactoryBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Service
class PeriodicDownloadOrchestrator internal constructor(
        private val feedService: YoutubeFeedService,
        timeToRun: ZonedDateTime
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val executor: ScheduledExecutorService
    private val timeToRun: ZonedDateTime
    var isRunning = false
        private set
    private var lastRun = LocalDateTime.MIN
    private var lastRunOutCome: String = "not run"

    @Autowired
    constructor(feedService: YoutubeFeedService) : this(feedService, midnight) {
    }

    private fun onException(thread: Thread, throwable: Throwable) {
        logger.error("Thread {} threw uncaught exception:", thread, throwable)
    }

    @PostConstruct
    fun start() {
        val millisecondsUntilMidnight = calculateDelayUntilMidnight()
        val fourInTheMorning = millisecondsUntilMidnight + Duration.of(4, ChronoUnit.HOURS).toMillis()
        executor.scheduleAtFixedRate({ addDownloadsTasksToExecutor() }, fourInTheMorning, MILLISECONDS_IN_A_DAY, TimeUnit.MILLISECONDS)
        logger.info("Started orchestrator, will run at {}", Instant.ofEpochMilli(System.currentTimeMillis() + fourInTheMorning).toString())
    }

    fun downloadEverythingOnce() {
        executor.execute { addDownloadsTasksToExecutor() }
    }

    @PreDestroy
    fun stop() {
        executor.shutdown()
    }

    fun lastRun(): LocalDateTime {
        return lastRun
    }

    fun lastRunOutCome(): String {
        return lastRunOutCome
    }

    private fun addDownloadsTasksToExecutor() {
        isRunning = true
        try {
            logger.info("Midnight, time to download")
            lastRun = LocalDateTime.now()
            feedService.updateChannelsAndVideos()
        } catch (e: Exception) {
            logger.error("Failed to download feeds", e)
            lastRunOutCome = e.message ?: "empty exception msg"
        } finally {
            lastRunOutCome = "OK"
            isRunning = false
        }
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