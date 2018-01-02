package com.richodemus.reader.common.kafka_adapter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.richodemus.reader.dto.EventId
import com.richodemus.reader.events_v2.Event
import com.richodemus.reader.events_v2.EventType
import com.richodemus.reader.events_v2.EventType.FEED_ADDED_TO_LABEL
import com.richodemus.reader.events_v2.EventType.LABEL_CREATED
import com.richodemus.reader.events_v2.EventType.PASSWORD_CHANGED
import com.richodemus.reader.events_v2.EventType.USER_CREATED
import com.richodemus.reader.events_v2.EventType.USER_SUBSCRIBED_TO_FEED
import com.richodemus.reader.events_v2.EventType.USER_UNWATCHED_ITEM
import com.richodemus.reader.events_v2.EventType.USER_WATCHED_ITEM
import com.richodemus.reader.events_v2.FeedAddedToLabel
import com.richodemus.reader.events_v2.LabelCreated
import com.richodemus.reader.events_v2.PasswordChanged
import com.richodemus.reader.events_v2.UserCreated
import com.richodemus.reader.events_v2.UserSubscribedToFeed
import com.richodemus.reader.events_v2.UserUnwatchedItem
import com.richodemus.reader.events_v2.UserWatchedItem
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer

private val mapper = jacksonObjectMapper()

class EventSerializer : Serializer<Event> {
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
    }

    override fun serialize(topic: String?, data: Event): ByteArray {
        return mapper.writeValueAsBytes(data)
    }

    override fun close() {
    }

}

class EventDeserializer : Deserializer<Event> {
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
    }

    override fun deserialize(topic: String?, data: ByteArray): Event {
        val str = String(data)
        return when {
            str isType USER_CREATED -> mapper.readValue<UserCreated>(data)
            str isType PASSWORD_CHANGED -> mapper.readValue<PasswordChanged>(data)
            str isType LABEL_CREATED -> mapper.readValue<LabelCreated>(data)
            str isType FEED_ADDED_TO_LABEL -> mapper.readValue<FeedAddedToLabel>(data)
            str isType USER_SUBSCRIBED_TO_FEED -> mapper.readValue<UserSubscribedToFeed>(data)
            str isType USER_WATCHED_ITEM -> mapper.readValue<UserWatchedItem>(data)
            str isType USER_UNWATCHED_ITEM -> mapper.readValue<UserUnwatchedItem>(data)
            else -> throw IllegalStateException()
        }
    }

    override fun close() {
    }

    private infix fun String.isType(type: EventType) = type.toString() in this // todo use regexp
}


class EventIdSerializer : Serializer<EventId> {
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
    }

    override fun serialize(topic: String?, data: EventId): ByteArray {
        return mapper.writeValueAsBytes(data)
    }

    override fun close() {
    }
}

class EventIdDeserializer : Deserializer<EventId> {
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
    }

    override fun deserialize(topic: String?, data: ByteArray): EventId {
        val content = String(data)
        val resut = mapper.readValue<EventId>(content)
        return resut
    }

    override fun close() {
    }
}
