package com.richodemus.reader.events

import com.richodemus.reader.dto.EventId

abstract class Event(val eventId: EventId, val type: EventType)
