package com.richodemus.reader.events

import com.richodemus.reader.dto.EventId
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.UserId

class SubscribeToFeed(eventId: EventId, val userId: UserId, val feedId: FeedId) : Event(eventId)
