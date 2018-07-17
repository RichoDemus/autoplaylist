package com.richodemus.autoplaylist.event

import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named
internal class InMemoryEventStore : EventStore {
    private val messageListeners = mutableListOf<(Event) -> Unit>()

    override fun consume(messageListener: (Event) -> Unit) {
        this.messageListeners.add(messageListener)
    }

    override fun produce(event: Event) {
        messageListeners.forEach { it(event) }
    }
}
