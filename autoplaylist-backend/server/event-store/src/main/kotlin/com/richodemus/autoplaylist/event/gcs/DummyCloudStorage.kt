package com.richodemus.autoplaylist.event.gcs

import kotlinx.coroutines.Deferred
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("default", "local")
class DummyCloudStorage : GoogleCloudStorage {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun read(): List<Pair<Long, Deferred<ByteArray>>> {
        logger.info("Reading some dummy events")
        return emptyList()
    }

    override fun write(filename: String, data: ByteArray) {
    }
}
