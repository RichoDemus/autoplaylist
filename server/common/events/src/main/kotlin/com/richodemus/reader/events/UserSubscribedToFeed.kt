package com.richodemus.reader.events

import com.richodemus.reader.dto.EventId
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.UserId

class UserSubscribedToFeed(eventId: EventId, val userId: UserId, val feedId: FeedId) : Event(eventId, EventType.USER_SUBSCRIBED_TO_FEED) {
    constructor(userId: UserId, feedId: FeedId) : this(EventId(), userId, feedId)

    override fun toString() = "User $userId subscribed to feed $feedId"
}
