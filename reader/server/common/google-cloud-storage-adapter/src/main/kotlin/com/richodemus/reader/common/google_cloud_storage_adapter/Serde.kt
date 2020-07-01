package com.richodemus.reader.common.google_cloud_storage_adapter

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

private val mapper = jacksonObjectMapper()

class EventSerializer {
     fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
    }

     fun serialize(data: Event): ByteArray {
        return mapper.writeValueAsBytes(data)
    }

     fun close() {
    }

}

class EventDeserializer {
     fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
    }

     fun deserialize(data: ByteArray): Event {
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

     fun close() {
    }

    private infix fun String.isType(type: EventType) = type.toString() in this // todo use regexp
}


class EventIdSerializer  {
     fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
    }

     fun serialize(topic: String?, data: EventId): ByteArray {
        return mapper.writeValueAsBytes(data)
    }

     fun close() {
    }
}

class EventIdDeserializer {
     fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
    }

     fun deserialize(topic: String?, data: ByteArray): EventId {
        val content = String(data)
        return mapper.readValue(content)
    }

     fun close() {
    }
}
