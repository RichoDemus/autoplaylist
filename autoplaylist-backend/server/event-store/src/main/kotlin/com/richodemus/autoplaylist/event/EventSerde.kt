package com.richodemus.autoplaylist.event

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.richodemus.autoplaylist.dto.events.Event
import com.richodemus.autoplaylist.dto.events.EventType
import com.richodemus.autoplaylist.dto.events.EventType.PLAYLIST_CREATED
import com.richodemus.autoplaylist.dto.events.EventType.PLAYLIST_RULES_CHANGED
import com.richodemus.autoplaylist.dto.events.EventType.REFRESH_TOKEN_UPDATED
import com.richodemus.autoplaylist.dto.events.EventType.USER_CREATED
import com.richodemus.autoplaylist.dto.events.EventType.USER_IDS_MAPPED
import com.richodemus.autoplaylist.dto.events.EventType.values
import com.richodemus.autoplaylist.dto.events.PlaylistCreated
import com.richodemus.autoplaylist.dto.events.PlaylistRulesChanged
import com.richodemus.autoplaylist.dto.events.RefreshTokenUpdated
import com.richodemus.autoplaylist.dto.events.UserCreated
import com.richodemus.autoplaylist.dto.events.UserIdsMapped
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named
internal class EventSerde {
    private val mapper = jacksonObjectMapper()

    fun serialize(event: Event): ByteArray {
        return mapper.writeValueAsBytes(event)
    }

    fun deserialize(data: ByteArray?): Event {
        val str = String(data ?: throw NullPointerException("Event is null..."))

        return when (str.type()) {
            USER_IDS_MAPPED -> mapper.readValue<UserIdsMapped>(data)
            USER_CREATED -> mapper.readValue<UserCreated>(data)
            REFRESH_TOKEN_UPDATED -> mapper.readValue<RefreshTokenUpdated>(data)
            PLAYLIST_CREATED -> mapper.readValue<PlaylistCreated>(data)
            PLAYLIST_RULES_CHANGED -> mapper.readValue<PlaylistRulesChanged>(data)
        }
    }

    private fun String.type(): EventType {
        values().forEach { eventType ->
            if (eventType.name in this) {
                return eventType
            }
        }
        throw IllegalStateException("Can't find EventType for event $this")
    }
}
