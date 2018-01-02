package com.richodemus.reader.label_service

import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.LabelId
import com.richodemus.reader.dto.LabelName
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.events_v2.Event
import com.richodemus.reader.events_v2.FeedAddedToLabel
import com.richodemus.reader.events_v2.LabelCreated

data class Label(val id: LabelId, val name: LabelName, val userId: UserId, val feeds: List<FeedId>) {
    constructor(label: LabelCreated) : this(label.labelId, label.labelName, label.userId, mutableListOf())

    internal fun process(evt: Event): Label {
        if (evt is FeedAddedToLabel && evt.labelId == id) {
            return this.copy(feeds = feeds.plus(evt.feedId))
        }
        return this
    }
}
