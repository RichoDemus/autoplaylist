package com.richodemus.reader.events

import com.richodemus.reader.dto.EventId
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.LabelId

class AddFeedToLabel(eventId: EventId, val labelId: LabelId, val feedId: FeedId) : Event(eventId, EventType.ADD_FEED_TO_LABEL) {
    override fun toString() = "Add feed $feedId to label $labelId"
}
