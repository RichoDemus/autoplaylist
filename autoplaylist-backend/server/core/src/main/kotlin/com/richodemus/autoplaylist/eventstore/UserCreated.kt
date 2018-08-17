package com.richodemus.autoplaylist.eventstore

import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.eventstore.EventType.USER_CREATED
import com.richodemus.autoplaylist.now

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
