package com.richodemus.reader.common.google_cloud_storage_adapter

import com.richodemus.reader.events_v2.Event
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread

@Service
internal class GoogleCloudStorageAdapter(
        private val persistence : Persistence
) : EventStore {
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
            persistence.readEvents().forEach { event ->
                val deserialized = EventDeserializer().deserialize(event.data.value.toByteArray())
                listeners.forEach { it(deserialized) }
                val expectedOffset = offset.incrementAndGet()
                if (event.offset.value != expectedOffset) {
                    logger.error("Expected offset {} got {}", expectedOffset, event.offset)
                }
            }
        }
    }

    override fun produce(event: Event) {
        val json = String(EventSerializer().serialize(event))

        val event1 = com.richodemus.reader.common.google_cloud_storage_adapter.Event(Offset(offset.incrementAndGet()), Key(event.id().value.toString()), Data(json))
        persistence.persist(event1)

        listeners.forEach { it(event) }
    }

    override fun close() {

    }
}
