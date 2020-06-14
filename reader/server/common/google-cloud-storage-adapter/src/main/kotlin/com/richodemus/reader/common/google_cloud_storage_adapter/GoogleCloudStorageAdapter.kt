package com.richodemus.reader.common.google_cloud_storage_adapter

import com.richodemus.reader.common.google_cloud_storage_adapter.Data
import com.richodemus.reader.common.google_cloud_storage_adapter.EventStore
import com.richodemus.reader.common.google_cloud_storage_adapter.Key
import com.richodemus.reader.common.google_cloud_storage_adapter.Offset
import com.richodemus.reader.events_v2.Event
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Singleton
import kotlin.concurrent.thread

@Singleton
class GoogleCloudStorageAdapter : EventStore {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val expectedListeners = 4
    private val offset = AtomicLong(-1L)
    private val listeners = mutableListOf<(Event) -> Unit>()

    override fun consume(messageListener: (Event) -> Unit) {
        listeners.add(messageListener)

        if (listeners.size > expectedListeners) {
            logger.error("Found {} listeners, expected {}", listeners.size, expectedListeners)
            throw IllegalStateException("Found ${listeners.size} listeners, expected $expectedListeners")
        }

        if (listeners.size < expectedListeners) {
            logger.info("Expecting more listeners, currently {}", listeners.size)
            return
        }

        logger.info("Got all listeners, time to start!")
        thread(name = "send-gcs-events-to-listeners") {
            val googleCloudStoragePersistence = GoogleCloudStoragePersistence()
            googleCloudStoragePersistence.readEvents().forEachRemaining { event ->
                val deserialized = EventDeserializer().deserialize(null, event.data.value.toByteArray())
                listeners.forEach { it(deserialized) }
                val expectedOffset = offset.incrementAndGet()
                if (event.offset.value != expectedOffset) {
                    logger.error("Expected offset {} got {}", expectedOffset, event.offset)
                }
            }
        }
    }

    override fun produce(event: Event) {
        val googleCloudStoragePersistence = GoogleCloudStoragePersistence()
        val json = String(EventSerializer().serialize(null, event))

        val event1 = com.richodemus.reader.common.google_cloud_storage_adapter.Event(Offset(offset.incrementAndGet()), Key(event.id().value.toString()), Data(json))
        googleCloudStoragePersistence.persist(event1)

        listeners.forEach { it(event) }
    }

    override fun close() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
