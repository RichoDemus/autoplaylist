package com.richodemus.autoplaylist.dto.events

import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.dto.events.EventType.REFRESH_TOKEN_UPDATED

data class RefreshTokenUpdated(
        val id: EventId = EventId(),
        val type: EventType = REFRESH_TOKEN_UPDATED,
        val timestamp: String = now(),
        val userId: UserId = UserId(),
        val refreshToken: RefreshToken
) : Event {
    override fun id() = id
    override fun type() = type
}
