package com.richodemus.reader.events

import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.UserId

class UserWatchedItem(val userId: UserId, val feedId: FeedId, val itemId: ItemId) : Event(type = EventType.USER_WATCHED_ITEM) {
    override fun toString() = "User $userId watched item $itemId in feed $feedId"
}
