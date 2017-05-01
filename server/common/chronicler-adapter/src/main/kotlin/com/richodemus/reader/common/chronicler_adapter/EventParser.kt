package com.richodemus.reader.common.chronicler_adapter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.richodemus.reader.events.AddFeedToLabel
import com.richodemus.reader.events.ChangePassword
import com.richodemus.reader.events.CreateLabel
import com.richodemus.reader.events.CreateUser
import com.richodemus.reader.events.Event
import com.richodemus.reader.events.EventType
import com.richodemus.reader.events.EventType.ADD_FEED_TO_LABEL
import com.richodemus.reader.events.EventType.CHANGE_PASSWORD
import com.richodemus.reader.events.EventType.CREATE_LABEL
import com.richodemus.reader.events.EventType.CREATE_USER
import com.richodemus.reader.events.EventType.USER_SUBSCRIBED_TO_FEED
import com.richodemus.reader.events.EventType.USER_UNWATCHED_ITEM
import com.richodemus.reader.events.EventType.USER_WATCHED_ITEM
import com.richodemus.reader.events.UserSubscribedToFeed
import com.richodemus.reader.events.UserUnwatchedItem
import com.richodemus.reader.events.UserWatchedItem

internal val mapper = ObjectMapper().apply { registerModule(KotlinModule()) }

internal fun String.toEvent(): Event {
    return when (figureOutType(this)) {
        CREATE_USER -> mapper.readValue(this, CreateUser::class.java)
        CHANGE_PASSWORD -> mapper.readValue(this, ChangePassword::class.java)
        CREATE_LABEL -> mapper.readValue(this, CreateLabel::class.java)
        ADD_FEED_TO_LABEL -> mapper.readValue(this, AddFeedToLabel::class.java)
        USER_SUBSCRIBED_TO_FEED -> mapper.readValue(this, UserSubscribedToFeed::class.java)
        USER_WATCHED_ITEM -> mapper.readValue(this, UserWatchedItem::class.java)
        USER_UNWATCHED_ITEM -> mapper.readValue(this, UserUnwatchedItem::class.java)
    }
}

// todo make this dynbamic by iterating trhrough enum
private fun figureOutType(eventString: String): EventType {
    EventType.values()
            .filter { eventString.contains(it.name) }
            .forEach { return it }
    throw IllegalStateException("Can't parse $eventString")
}