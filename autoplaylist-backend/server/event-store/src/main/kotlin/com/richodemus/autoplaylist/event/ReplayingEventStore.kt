package com.richodemus.autoplaylist.event

import com.richodemus.autoplaylist.event.gcs.GoogleCloudStorageAdapter
import com.richodemus.autoplaylist.dto.events.Event
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Event store that replays all previous messages to each new consumer
 */
@Component
class ReplayingEventStore internal constructor(
        private val googleCloudStorageAdapter: GoogleCloudStorageAdapter,
        registry: MeterRegistry
) : EventStore, CoroutineScope {
    private val logger = LoggerFactory.getLogger(javaClass)
    override val coroutineContext = Dispatchers.Default
    private var events: List<Event> = emptyList()
    private var actors: List<SendChannel<Any>> = emptyList()

    init {
        registry.gauge("events", events) { it.size.toDouble() }
        events += runBlocking { googleCloudStorageAdapter.read().asSequence() }
        logger.info("Got ${events.size} events")
    }

    // seems like actors are deprecated, will see what happens
    @ExperimentalCoroutinesApi
    @ObsoleteCoroutinesApi
    @Synchronized
    override fun consume(onEvent: suspend (Event) -> Unit) {
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
        logger.info("New event: $event")
        googleCloudStorageAdapter.save(event)
        events += event
        runBlocking { actors.forEach { it.send(Any()) } }
    }
}
