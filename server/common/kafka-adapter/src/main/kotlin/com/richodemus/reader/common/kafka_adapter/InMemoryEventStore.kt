package com.richodemus.reader.common.kafka_adapter

import com.richodemus.reader.events_v2.Event
import javax.inject.Singleton

/**
 * Mock used for testing
 */
@Singleton
class InMemoryEventStore : EventStore {
    private val messageListeners = mutableListOf<(Event) -> Unit>()

    override fun consume(messageListener: (Event) -> Unit) {
        this.messageListeners.add(messageListener)
    }

    override fun produce(event: Event) {
        messageListeners.forEach { it(event) }
    }

    override fun close() {
    }
}
