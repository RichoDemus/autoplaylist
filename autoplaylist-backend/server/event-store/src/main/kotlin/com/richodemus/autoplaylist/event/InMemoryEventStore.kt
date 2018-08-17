package com.richodemus.autoplaylist.event

import com.richodemus.autoplaylist.eventstore.Event
import com.richodemus.autoplaylist.eventstore.EventStore
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
@Profile("default", "local")
internal class InMemoryEventStore @Inject constructor(registry: MeterRegistry) : EventStore {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val messageListeners = mutableListOf<(Event) -> Unit>()

    init {
        registry.gauge("eventlisteners", messageListeners) { it.size.toDouble() }
    }

    override fun consume(onEvent: (Event) -> Unit) {
        this.messageListeners.add(onEvent)
    }

    override fun produce(event: Event) {
        logger.info("New Event: {}", event)
        messageListeners.forEach { it(event) }
    }
}
