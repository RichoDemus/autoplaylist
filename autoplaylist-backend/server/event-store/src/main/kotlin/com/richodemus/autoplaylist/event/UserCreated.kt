package com.richodemus.autoplaylist.event

import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.event.EventType.USER_CREATED

data class UserCreated(
        val id: EventId = EventId(),
        val type: EventType = USER_CREATED,
        val timestamp: String = now(),
        val userId: UserId = UserId(),
        val spotifyUserId: SpotifyUserId
) : Event {
    override fun id() = id
    override fun type() = type
}
