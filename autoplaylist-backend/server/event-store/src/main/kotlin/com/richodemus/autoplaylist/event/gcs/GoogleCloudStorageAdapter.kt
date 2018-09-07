package com.richodemus.autoplaylist.event.gcs

import com.richodemus.autoplaylist.event.EventSerde
import com.richodemus.autoplaylist.eventstore.Event
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.LongAdder
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named
internal class GoogleCloudStorageAdapter(
        private val eventSerde: EventSerde,
        private val gcsClient: GoogleCloudStorage
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val nextOffset = AtomicLong(0L)

    internal suspend fun read(): Iterator<Event> {
        var run = true
        val eventsStarted = LongAdder()
        val eventsDownloaded = LongAdder()
        var eventsDownloadedAtLastPrint = 0L
        try {
            logger.info("Preparing to download events from Google Cloud Storage...")

            launch {
                while (run) {
                    if (eventsStarted.sum() > 0 || eventsDownloaded.sum() > 0) {
                        val downloaded = eventsDownloaded.sum()
                        val eventsPerSecond = (downloaded - eventsDownloadedAtLastPrint)
                        logger.info("Event downloads started: ${eventsStarted.sum()}, Events downloaded: ${eventsDownloaded.sum()}, Events per second: $eventsPerSecond")
                        eventsDownloadedAtLastPrint = downloaded
                    } else {
                        logger.info("Getting all filenames from GCS...")
                        delay(4_000L)
                    }
                    delay(1_000L)
                }
            }

            return gcsClient.read()
                    .map {
                        eventsStarted.increment()
                        it
                    }.map { pair ->
                        val event = pair.second.await()
                        eventsDownloaded.increment()
                        nextOffset.incrementAndGet()
                        pair.first to event
                    }.map { pair ->
                        pair.mapSecond { eventSerde.deserialize(it) }
                    }
                    .sortedBy { it.first }
                    .map { it.second }
                    .iterator()
        } finally {
            run = false
        }
    }

    @Synchronized
    internal fun save(event: Event) {
        val filename = nextOffset.getAndIncrement().toString()
        val data = eventSerde.serialize(event)
        gcsClient.write(filename, data)
    }

    private fun <A, B, C> Pair<A, B>.mapSecond(function: (B) -> C): Pair<A, C> {
        return this.first to function(this.second)
    }
}
