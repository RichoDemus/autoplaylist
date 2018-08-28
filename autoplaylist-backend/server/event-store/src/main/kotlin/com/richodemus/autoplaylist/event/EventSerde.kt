package com.richodemus.autoplaylist.event

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.richodemus.autoplaylist.eventstore.Event
import com.richodemus.autoplaylist.eventstore.EventType
import com.richodemus.autoplaylist.eventstore.EventType.PLAYLIST_CREATED
import com.richodemus.autoplaylist.eventstore.EventType.PLAYLIST_RULES_CHANGED
import com.richodemus.autoplaylist.eventstore.EventType.REFRESH_TOKEN_UPDATED
import com.richodemus.autoplaylist.eventstore.EventType.USER_CREATED
import com.richodemus.autoplaylist.eventstore.EventType.values
import com.richodemus.autoplaylist.eventstore.PlaylistCreated
import com.richodemus.autoplaylist.eventstore.PlaylistRulesChanged
import com.richodemus.autoplaylist.eventstore.RefreshTokenUpdated
import com.richodemus.autoplaylist.eventstore.UserCreated
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
