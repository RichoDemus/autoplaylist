package com.richodemus.autoplaylist.eventstore

import com.richodemus.autoplaylist.dto.PlaylistId
import com.richodemus.autoplaylist.dto.PlaylistName
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.eventstore.EventType.PLAYLIST_CREATED
import com.richodemus.autoplaylist.now

data class PlaylistCreated(
        val id: EventId = EventId(),
        val type: EventType = PLAYLIST_CREATED,
        val timestamp: String = now(),
        val playlistId: PlaylistId = PlaylistId(),
        val userId: UserId,
        val playlistName: PlaylistName
) : Event {
    override fun id() = id
    override fun type() = type
}
