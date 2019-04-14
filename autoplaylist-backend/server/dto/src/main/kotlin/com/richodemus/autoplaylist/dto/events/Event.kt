package com.richodemus.autoplaylist.dto.events

interface Event {
    fun id(): EventId
    fun type(): EventType
}
