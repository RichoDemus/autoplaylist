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

internal val mapper = ObjectMapper().apply { registerModule(KotlinModule()) }

internal fun String.toEvent() : Event {
    return when (figureOutType(this)) {
        CREATE_USER -> mapper.readValue(this, CreateUser::class.java)
        CHANGE_PASSWORD -> mapper.readValue(this, ChangePassword::class.java)
        CREATE_LABEL -> mapper.readValue(this, CreateLabel::class.java)
        ADD_FEED_TO_LABEL -> mapper.readValue(this, AddFeedToLabel::class.java)
    }
}

private fun figureOutType(eventString: String): EventType {
     if (eventString.contains("CREATE_USER")) {
         return CREATE_USER
     }
     if (eventString.contains("CHANGE_PASSWORD")) {
         return CHANGE_PASSWORD
     }
     if (eventString.contains("CREATE_LABEL")) {
         return CREATE_LABEL
     }
     if (eventString.contains("ADD_FEED_TO_LABEL")) {
         return ADD_FEED_TO_LABEL
     }
     throw IllegalStateException("Can't parse $eventString")
 }