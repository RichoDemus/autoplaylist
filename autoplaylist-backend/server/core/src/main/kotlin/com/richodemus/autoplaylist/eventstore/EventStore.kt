package com.richodemus.autoplaylist.eventstore

interface EventStore {
    fun consume(onEvent: (Event) -> Unit)
    fun produce(event: Event)
}
