package com.richodemus.autoplaylist.dto.events

import com.richodemus.autoplaylist.dto.PlaylistId
import com.richodemus.autoplaylist.dto.events.EventType.PLAYLIST_RULES_CHANGED

data class PlaylistSyncChanged(
        val id: EventId = EventId(),
        val type: EventType = PLAYLIST_RULES_CHANGED,
        val timestamp: String = now(),
        val playlistId: PlaylistId,
        val sync: Boolean
) : Event {
    override fun id() = id
    override fun type() = type
}
