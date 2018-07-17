package com.richodemus.autoplaylist.event

interface Event {
    fun id(): EventId
    fun type(): EventType
}
