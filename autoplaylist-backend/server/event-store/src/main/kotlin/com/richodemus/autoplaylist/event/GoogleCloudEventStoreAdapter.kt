package com.richodemus.autoplaylist.event

import com.richodemus.autoplaylist.eventstore.Event
import com.richodemus.autoplaylist.eventstore.EventStore
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.concurrent.thread

@Component
@Profile("prod")
internal class GoogleCloudEventStoreAdapter(
        private val gcsClient: GoogleCloudStorageClient,
        registry: MeterRegistry
) : EventStore {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val expectedListeners = 1
    private val listeners = mutableListOf<(Event) -> Unit>()
    private val events = registry.counter("events")

    override fun consume(onEvent: (Event) -> Unit) {
        listeners.add(onEvent)

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
            gcsClient.read().forEachRemaining { event ->
                events.increment()
                listeners.forEach { it(event) }
            }
        }
    }

    override fun produce(event: Event) {
        logger.info("Saving event $event")
        gcsClient.save(event)
        events.increment()
        listeners.forEach { it(event) }
    }
}
