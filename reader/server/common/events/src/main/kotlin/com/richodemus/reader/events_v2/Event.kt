package com.richodemus.reader.events_v2

import com.richodemus.reader.dto.EventId

interface Event {
    fun id(): EventId
    fun type(): EventType
}
