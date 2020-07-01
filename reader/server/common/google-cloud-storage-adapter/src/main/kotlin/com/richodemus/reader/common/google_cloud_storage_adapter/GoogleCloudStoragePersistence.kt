package com.richodemus.reader.common.google_cloud_storage_adapter

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageOptions
import com.richodemus.reader.common.google_cloud_storage_adapter.Data
import com.richodemus.reader.common.google_cloud_storage_adapter.Event
import com.richodemus.reader.common.google_cloud_storage_adapter.Key
import com.richodemus.reader.common.google_cloud_storage_adapter.Offset
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.atomic.LongAdder
import java.util.function.Supplier
import kotlin.concurrent.thread

@Profile(value = ["!dev", "!test"])
@Component
internal class GoogleCloudStoragePersistence(
        @Value("\${gcp.project}") val gcsProject:String,
        @Value("\${gcp.bucket}") val gcsBucket:String
) : Persistence {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val directory = "events/v2/"

    private val service = StorageOptions.newBuilder()
            .setProjectId(gcsProject)
            .build()
            .service

    override fun readEvents(): Sequence<Event> {
        val threads = Runtime.getRuntime().availableProcessors() * 10
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
                logger.info("Done Downloading!")
            }

            return service.list(gcsBucket)
                    .iterateAll()
                    .filter { it.blobId.name.startsWith(directory) }
                    .map {
                        eventsStarted.increment()
                        CompletableFuture.supplyAsync(Supplier {
                            val offset = it.blobId.name.split("/")[2].toLong()
                            val data = it.getContent().let { String(it) }
                            val key = data.substringBefore(",")
                            val event = data.substringAfter(",")
                            Event(Offset(offset), Key(key), Data(event))
                        }, executor)
                    }
                    .map {
                        eventsDownloaded.increment()
                        it.get()
                    }
                    .sortedBy { it.offset.value }
                    .asSequence() // if we do asSequence before get the executor will have been closed

        } finally {
            run = false
            executor.shutdown()
        }
    }

    override fun persist(event: Event) {
        val filename = "$directory${event.offset.value}"
        val data = "${event.key.value},${event.data.value}"
        val eventBytes = data.toByteArray()
        val blob = BlobId.of(gcsBucket, filename)
        if (exists(blob)) {
            logger.info("File $filename already exists in GCS, skipping...")
            return
        }
        service.create(BlobInfo.newBuilder(blob).build(), eventBytes)
//        logger.info("Would've saved $filename: ${String(eventBytes)}")
    }

    private fun exists(blob: BlobId) = service.get(blob) != null
}
