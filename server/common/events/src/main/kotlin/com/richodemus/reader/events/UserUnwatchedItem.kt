package com.richodemus.reader.events

import com.richodemus.reader.dto.EventId
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.UserId

class UserUnwatchedItem(eventId: EventId, val userId: UserId, val feedId: FeedId, val itemId: ItemId) : Event(eventId, EventType.USER_UNWATCHED_ITEM) {
    constructor(userId: UserId, feedId: FeedId, itemId: ItemId) : this(EventId(), userId, feedId, itemId)

    override fun toString() = "User $userId un-watched item $itemId in feed $feedId"
}
