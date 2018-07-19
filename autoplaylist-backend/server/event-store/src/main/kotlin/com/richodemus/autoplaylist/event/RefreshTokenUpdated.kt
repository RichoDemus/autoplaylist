package com.richodemus.autoplaylist.event

import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.UserId

data class RefreshTokenUpdated(
        val id: EventId = EventId(),
        val type: EventType = EventType.REFRESH_TOKEN_UPDATED,
        val timestamp: String = now(),
        val userId: UserId = UserId(),
        val refreshToken: RefreshToken
) : Event {
    override fun id() = id
    override fun type() = type
}
