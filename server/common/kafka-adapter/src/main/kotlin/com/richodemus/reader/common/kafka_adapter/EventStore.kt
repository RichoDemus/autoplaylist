package com.richodemus.reader.common.kafka_adapter

import com.richodemus.reader.events.Event
import java.io.Closeable

interface EventStore : Closeable {
    fun consume(messageListener: (Event) -> Unit)
    fun produce(event: Event)
}
