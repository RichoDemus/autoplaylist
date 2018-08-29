package com.richodemus.autoplaylist.event

import com.richodemus.autoplaylist.eventstore.Event
import io.github.vjames19.futures.jdk8.map
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.LongAdder
import javax.inject.Named
import javax.inject.Singleton
import kotlin.concurrent.thread

@Singleton
@Named
internal class GoogleCloudStorageAdapter(
        private val eventSerde: EventSerde,
        private val gcsClient: GoogleCloudStorageClient
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val nextOffset = AtomicLong(0L)

    internal fun read(): Iterator<Event> {
        val threads = Runtime.getRuntime().availableProcessors() * 100
        val executor = Executors.newFixedThreadPool(threads)
        var run = true
        val eventsStarted = LongAdder()
        val eventsDownloaded = LongAdder()
        var eventsDownloadedAtLastPrint = 0L
        try {
            logger.info("Preparing to download events from Google Cloud Storage, using {} threads...", threads)

            thread(name = "print-download-progress") {
                while (run) {
                    if (eventsStarted.sum() > 0 || eventsDownloaded.sum() > 0) {
                        val downloaded = eventsDownloaded.sum()
                        val eventsPerSecond = (downloaded - eventsDownloadedAtLastPrint)
                        logger.info("Event downloads started: ${eventsStarted.sum()}, Events downloaded: ${eventsDownloaded.sum()}, Events per second: $eventsPerSecond")
                        eventsDownloadedAtLastPrint = downloaded
                    } else {
                        logger.info("Getting all filenames from GCS...")
                        Thread.sleep(4_000L)
                    }
                    Thread.sleep(1_000L)
                }
            }

            return gcsClient.read().join()
                    .map {
                        eventsStarted.increment()
                        it
                    }.map { future ->
                        future.first to future.second.map {
                            eventSerde.deserialize(it)
                        }
                    }.map { pair ->
                        val event = pair.second.join()
                        eventsDownloaded.increment()
                        nextOffset.incrementAndGet()
                        pair.first to event
                    }.sortedBy { it.first }
                    .map { it.second }
                    .iterator()
        } finally {
            run = false
            executor.shutdown()
        }
    }

    @Synchronized
    internal fun save(event: Event) {
        val filename = nextOffset.getAndIncrement().toString()
        val data = eventSerde.serialize(event)
        gcsClient.write(filename, data)
    }
}
