package com.richodemus.autoplaylist.eventstore

import com.richodemus.autoplaylist.dto.PlaylistId
import com.richodemus.autoplaylist.dto.Rules
import com.richodemus.autoplaylist.eventstore.EventType.PLAYLIST_RULES_CHANGED
import com.richodemus.autoplaylist.now

data class PlaylistRulesChanged(
        val id: EventId = EventId(),
        val type: EventType = PLAYLIST_RULES_CHANGED,
        val timestamp: String = now(),
        val playlistId: PlaylistId,
        val rules: Rules
) : Event {
    override fun id() = id
    override fun type() = type
}