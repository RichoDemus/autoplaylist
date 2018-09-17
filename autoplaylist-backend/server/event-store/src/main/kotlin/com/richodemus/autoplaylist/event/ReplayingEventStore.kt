package com.richodemus.autoplaylist.event

import com.richodemus.autoplaylist.event.gcs.GoogleCloudStorageAdapter
import com.richodemus.autoplaylist.eventstore.Event
import com.richodemus.autoplaylist.eventstore.EventStore
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.runBlocking
import org.springframework.stereotype.Component

/**
 * Event store that replays all previous messages to each new consumer
 */
@Component
internal class ReplayingEventStore(
        private val googleCloudStorageAdapter: GoogleCloudStorageAdapter,
        registry: MeterRegistry
) : EventStore {
    private var events: List<Event> = emptyList()
    private var actors: List<SendChannel<Any>> = emptyList()

    init {
        registry.gauge("events", events) { it.size.toDouble() }
        events += runBlocking { googleCloudStorageAdapter.read().asSequence() }
    }

    @Synchronized
    override fun consume(onEvent: (Event) -> Unit) {
        actors += actor {
            var nextMessage = 0
            while (!channel.isClosedForSend) {
                if (events.size > nextMessage) {
                    onEvent(events[nextMessage++])
                } else {
                    try {
                        channel.receive()
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    @Synchronized
    override fun produce(event: Event) {
        googleCloudStorageAdapter.save(event)
        events += event
        runBlocking { actors.forEach { it.send(Any()) } }
    }
}