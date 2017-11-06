package com.richodemus.reader.events

import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.LabelId

class AddFeedToLabel(val labelId: LabelId, val feedId: FeedId) : Event(type = EventType.ADD_FEED_TO_LABEL) {
    override fun toString() = "Add feed $feedId to label $labelId"
}
