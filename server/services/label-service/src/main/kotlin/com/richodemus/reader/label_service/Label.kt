package com.richodemus.reader.label_service

import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.LabelId
import com.richodemus.reader.dto.LabelName
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.events.AddFeedToLabel
import com.richodemus.reader.events.CreateLabel
import com.richodemus.reader.events.Event

data class Label(val id: LabelId, val name: LabelName, val userId: UserId, val feeds: List<FeedId>) {
    constructor(label: CreateLabel) : this(label.labelId, label.labelName, label.userId, mutableListOf())

    internal fun process(evt: Event): Label {
        if (evt is AddFeedToLabel && evt.labelId == id) {
            return this.copy(feeds = feeds.plus(evt.feedId))
        }
        return this
    }
}
