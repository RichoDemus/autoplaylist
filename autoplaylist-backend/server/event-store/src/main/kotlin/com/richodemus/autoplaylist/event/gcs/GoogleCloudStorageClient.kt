package com.richodemus.autoplaylist.event.gcs

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.inject.Named

@Component
@Profile("prod")
internal class GoogleCloudStorageClient(
        @Named("gcsProject") private val gcsProject: String,
        @Named("gcsBucket") private val gcsBucket: String
) : GoogleCloudStorage, CoroutineScope {
    private val logger = LoggerFactory.getLogger(javaClass)
    override val coroutineContext = Dispatchers.Default
    private val directory = "events/v2/"

    private val service = StorageOptions.newBuilder()
            .setProjectId(gcsProject)
            .build()
            .service

    init {
        logger.info("GCS Config: $gcsProject, $gcsBucket")
    }

    override fun read(): List<Pair<Long, Deferred<ByteArray>>> {
        return service.list(gcsBucket)
                .iterateAll()
                .filter { it.blobId.name.startsWith(directory) }
                .map {
                    val sequenceNumber = it.blobId.name.split("/")[2].toLong()
                    sequenceNumber to async { it.getContent() }
                }
    }

    override fun write(filename: String, data: ByteArray) {
        val blob = BlobId.of(gcsBucket, "$directory$filename")
        if (exists(blob)) {
            logger.info("File $filename already exists in GCS, skipping...")
            return
        }
        service.create(BlobInfo.newBuilder(blob).build(), data)
    }

    private fun exists(blob: BlobId) = service.get(blob) != null
}
