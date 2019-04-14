package com.richodemus.autoplaylist.event

import com.richodemus.autoplaylist.dto.events.Event

interface EventStore {
    fun consume(onEvent: suspend (Event) -> Unit)
    fun produce(event: Event)
}
