package com.richodemus.autoplaylist.dto.events

import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.dto.events.EventType.USER_CREATED

data class UserCreated(
        val id: EventId = EventId(),
        val type: EventType = USER_CREATED,
        val timestamp: String = now(),
        val userId: UserId = UserId()
) : Event {
    override fun id() = id
    override fun type() = type
}
