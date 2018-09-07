package com.richodemus.autoplaylist.event.gcs

import kotlinx.coroutines.experimental.Deferred
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("default", "local")
class DummyCloudStorage : GoogleCloudStorage {
    override fun read(): List<Pair<Long, Deferred<ByteArray>>> {
        return emptyList()
    }

    override fun write(filename: String, data: ByteArray) {
    }
}
