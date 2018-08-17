package com.richodemus.autoplaylist.eventstore

interface Event {
    fun id(): EventId
    fun type(): EventType
}
