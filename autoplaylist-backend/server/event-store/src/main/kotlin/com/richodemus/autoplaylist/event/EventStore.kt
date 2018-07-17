package com.richodemus.autoplaylist.event

interface EventStore {
    fun consume(messageListener: (Event) -> Unit)
    fun produce(event: Event)
}
