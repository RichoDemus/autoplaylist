package com.richodemus.autoplaylist.event

import io.micrometer.core.instrument.MeterRegistry
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named
internal class InMemoryEventStore @Inject constructor(registry: MeterRegistry) : EventStore {
    private val messageListeners = mutableListOf<(Event) -> Unit>()
    private val temporaryListeners = mutableListOf<(Event) -> Boolean>()

    init {
        registry.gauge("temporary.listeners", temporaryListeners) { it.size.toDouble() }
    }

    override fun consume(messageListener: (Event) -> Unit) {
        this.messageListeners.add(messageListener)
    }

    override fun produce(event: Event) {
        messageListeners.forEach { it(event) }

        val successfulListeners = temporaryListeners.map { listener ->
            listener to listener(event)
        }.filter { it.second }.map { it.first }

        temporaryListeners.removeAll(successfulListeners)
    }

    override fun addTemporaryListener(messageListener: (Event) -> Boolean) {
        temporaryListeners.add(messageListener)
    }
}
