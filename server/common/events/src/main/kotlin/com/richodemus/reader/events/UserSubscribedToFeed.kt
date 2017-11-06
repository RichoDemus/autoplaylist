package com.richodemus.reader.events

import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.UserId

class UserSubscribedToFeed(val userId: UserId, val feedId: FeedId) : Event(type = EventType.USER_SUBSCRIBED_TO_FEED) {
    override fun toString() = "User $userId subscribed to feed $feedId"
}
