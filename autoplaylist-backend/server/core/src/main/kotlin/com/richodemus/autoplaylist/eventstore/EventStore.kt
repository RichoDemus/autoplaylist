package com.richodemus.autoplaylist.eventstore

interface EventStore {
    fun consume(onEvent: suspend (Event) -> Unit)
    fun produce(event: Event)
}
