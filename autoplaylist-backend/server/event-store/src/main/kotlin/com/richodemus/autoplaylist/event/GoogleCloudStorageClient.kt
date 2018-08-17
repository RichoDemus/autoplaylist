package com.richodemus.autoplaylist.event

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageOptions
import com.richodemus.autoplaylist.eventstore.Event
import io.github.vjames19.futures.jdk8.Future
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.LongAdder
import javax.inject.Named
import javax.inject.Singleton
import kotlin.concurrent.thread

@Singleton
@Named
internal class GoogleCloudStorageClient
(
        private val eventSerde: EventSerde,
        @Named("gcsProject") private val gcsProject: String,
        @Named("gcsBucket") private val gcsBucket: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val directory = "events/v1/"
    private val nextOffset = AtomicLong(0L)
    private val service = StorageOptions.newBuilder()
            .setProjectId(gcsProject)
            .build()
            .service

    init {
        logger.info("GCS Config: $gcsProject, $gcsBucket")
    }

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

            return service.list(gcsBucket)
                    .iterateAll()
                    .filter { it.blobId.name.startsWith(directory) }
                    .map {
                        eventsStarted.increment()
                        Future(executor) {
                            val offset = it.blobId.name.split("/")[2].toLong()
                            offset to eventSerde.deserialize(it.getContent())
                        }
                    }
                    .map {
                        eventsDownloaded.increment()
                        nextOffset.incrementAndGet()
                        it.get()
                    }
                    .sortedBy { it.first }
                    .map { it.second }
                    .iterator()

        } finally {
            run = false
            executor.shutdown()
        }
    }

    @Synchronized
    internal fun save(event: Event) {
        val filename = "$directory${nextOffset.getAndIncrement()}"
        val data = eventSerde.serialize(event)
        val blob = BlobId.of(gcsBucket, filename)
        if (exists(blob)) {
            logger.info("File $filename already exists in GCS, skipping...")
            return
        }
        service.create(BlobInfo.newBuilder(blob).build(), data)
    }

    private fun exists(blob: BlobId) = service.get(blob) != null
}
