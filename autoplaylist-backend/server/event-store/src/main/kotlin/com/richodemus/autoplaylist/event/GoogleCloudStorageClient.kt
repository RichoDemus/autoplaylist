package com.richodemus.autoplaylist.event

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageOptions
import io.github.vjames19.futures.jdk8.Future
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named
internal class GoogleCloudStorageClient(
        @Named("gcsProject") private val gcsProject: String,
        @Named("gcsBucket") private val gcsBucket: String
) : GoogleCloudStorage {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val directory = "events/v1/"

    private val service = StorageOptions.newBuilder()
            .setProjectId(gcsProject)
            .build()
            .service

    init {
        logger.info("GCS Config: $gcsProject, $gcsBucket")
    }

    override fun read(): CompletableFuture<List<Pair<Long, CompletableFuture<ByteArray>>>> {
        return Future {
            service.list(gcsBucket)
                    .iterateAll()
                    .filter { it.blobId.name.startsWith(directory) }
                    .map {
                        val sequenceNumber = it.blobId.name.split("/")[2].toLong()
                        sequenceNumber to Future { it.getContent() }
                    }
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
