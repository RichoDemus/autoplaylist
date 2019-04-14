package com.richodemus.autoplaylist.dto.events

import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.dto.events.EventType.USER_IDS_MAPPED

class UserIdsMapped private constructor(
        val id: EventId,
        val type: EventType,
        val timestamp: String,
        val userId: UserId,
        val spotifyUserId: SpotifyUserId
) : Event {
    companion object {
        fun create(spotifyUserId: SpotifyUserId) = UserIdsMapped(
                EventId(),
                USER_IDS_MAPPED,
                now(),
                UserId(),
                spotifyUserId
        )
    }

    override fun id() = id

    override fun type() = USER_IDS_MAPPED
}
