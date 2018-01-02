package com.richodemus.reader.events_v2

import com.richodemus.reader.dto.EventId
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.LabelId
import com.richodemus.reader.dto.LabelName
import com.richodemus.reader.dto.PasswordHash
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import com.richodemus.reader.events_v2.EventType.FEED_ADDED_TO_LABEL
import com.richodemus.reader.events_v2.EventType.LABEL_CREATED
import com.richodemus.reader.events_v2.EventType.PASSWORD_CHANGED
import com.richodemus.reader.events_v2.EventType.USER_CREATED
import com.richodemus.reader.events_v2.EventType.USER_SUBSCRIBED_TO_FEED
import com.richodemus.reader.events_v2.EventType.USER_UNWATCHED_ITEM
import com.richodemus.reader.events_v2.EventType.USER_WATCHED_ITEM
import java.time.ZoneOffset
import java.time.ZonedDateTime

data class UserCreated(val id: EventId,
                       val timestamp: String,
                       val type: EventType,
                       val userId: UserId,
                       val username: Username,
                       val password: PasswordHash) : Event {
    constructor(userId: UserId, username: Username, passwordHash: PasswordHash) :
            this(EventId(), now(), USER_CREATED, userId, username, passwordHash)

    override fun id() = id
    override fun type() = type

}

data class PasswordChanged(val id: EventId,
                           val timestamp: String,
                           val type: EventType,
                           val userId: UserId,
                           val password: PasswordHash) : Event {

    constructor(userId: UserId, passwordHash: PasswordHash) :
            this(EventId(), now(), PASSWORD_CHANGED, userId, passwordHash)

    override fun id() = id

    override fun type() = type
}

data class LabelCreated(val id: EventId,
                        val timestamp: String,
                        val type: EventType,
                        val userId: UserId,
                        val labelId: LabelId,
                        val labelName: LabelName) : Event {

    constructor(labelId: LabelId, labelName: LabelName, userId: UserId) :
            this(EventId(), now(), LABEL_CREATED, userId, labelId, labelName)

    override fun id() = id

    override fun type() = type
}

data class FeedAddedToLabel(val id: EventId,
                            val timestamp: String,
                            val type: EventType,
                            val labelId: LabelId,
                            val feedId: FeedId) : Event {

    constructor(labelId: LabelId, feedId: FeedId) :
            this(EventId(), now(), FEED_ADDED_TO_LABEL, labelId, feedId)

    override fun id() = id

    override fun type() = type
}

data class UserSubscribedToFeed(val id: EventId,
                                val timestamp: String,
                                val type: EventType,
                                val userId: UserId,
                                val feedId: FeedId) : Event {

    constructor(userId: UserId, feedId: FeedId) :
            this(EventId(), now(), USER_SUBSCRIBED_TO_FEED, userId, feedId)

    override fun id() = id

    override fun type() = type
}

data class UserWatchedItem(val id: EventId,
                           val timestamp: String,
                           val type: EventType,
                           val userId: UserId,
                           val feedId: FeedId,
                           val itemId: ItemId) : Event {

    constructor(userId: UserId, feedId: FeedId, itemId: ItemId) :
            this(EventId(), now(), USER_WATCHED_ITEM, userId, feedId, itemId)

    override fun id() = id

    override fun type() = type
}

data class UserUnwatchedItem(val id: EventId,
                             val timestamp: String,
                             val type: EventType,
                             val userId: UserId,
                             val feedId: FeedId,
                             val itemId: ItemId) : Event {

    constructor(userId: UserId, feedId: FeedId, itemId: ItemId) :
            this(EventId(), now(), USER_UNWATCHED_ITEM, userId, feedId, itemId)

    override fun id() = id

    override fun type() = type
}

private fun now() = ZonedDateTime.now(ZoneOffset.UTC).toString()
