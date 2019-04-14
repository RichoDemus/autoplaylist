package com.richodemus.autoplaylist.dto.events

import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.dto.events.EventType.USER_CREATED

data class UserCreated(
        val id: EventId = EventId(),
        val type: EventType = USER_CREATED,
        val timestamp: String = now(),
        val userId: UserId = UserId(),
        val spotifyUserId: SpotifyUserId,
        val refreshToken: RefreshToken
) : Event {
    override fun id() = id
    override fun type() = type
}
