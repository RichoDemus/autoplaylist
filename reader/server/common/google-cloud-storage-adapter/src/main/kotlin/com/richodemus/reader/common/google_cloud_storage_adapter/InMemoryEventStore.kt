package com.richodemus.reader.common.google_cloud_storage_adapter

import com.richodemus.reader.events_v2.Event

/**
 * Mock used for testing
 */
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
