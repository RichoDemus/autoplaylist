package com.richodemus.reader.common.kafka_adapter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.richodemus.reader.events.AddFeedToLabel
import com.richodemus.reader.events.ChangePassword
import com.richodemus.reader.events.CreateLabel
import com.richodemus.reader.events.CreateUser
import com.richodemus.reader.events.Event
import com.richodemus.reader.events.EventType
import com.richodemus.reader.events.UserSubscribedToFeed
import com.richodemus.reader.events.UserUnwatchedItem
import com.richodemus.reader.events.UserWatchedItem

internal val mapper = ObjectMapper().apply { registerModule(KotlinModule()) }
internal fun String.toEvent(): Event {
    val type = figureOutType(this)
    return when (type) {
        EventType.CREATE_USER -> mapper.readValue(this, CreateUser::class.java)
        EventType.CHANGE_PASSWORD -> mapper.readValue(this, ChangePassword::class.java)
        EventType.CREATE_LABEL -> mapper.readValue(this, CreateLabel::class.java)
        EventType.ADD_FEED_TO_LABEL -> mapper.readValue(this, AddFeedToLabel::class.java)
        EventType.USER_SUBSCRIBED_TO_FEED -> mapper.readValue(this, UserSubscribedToFeed::class.java)
        EventType.USER_WATCHED_ITEM -> mapper.readValue(this, UserWatchedItem::class.java)
        EventType.USER_UNWATCHED_ITEM -> mapper.readValue(this, UserUnwatchedItem::class.java)
    }
}

private fun figureOutType(eventString: String): EventType {
    EventType.values()
            .filter { eventString.contains(it.name) }
            .forEach { return it }
    throw IllegalStateException("Can't parse $eventString")
}
