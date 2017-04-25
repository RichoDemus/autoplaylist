package com.richodemus.reader.label_service

import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.LabelId
import com.richodemus.reader.dto.LabelName
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.events.AddFeedToLabel
import com.richodemus.reader.events.CreateLabel

class Label(val id: LabelId, val name: LabelName, val userId: UserId, val feeds: MutableList<FeedId>) {
    constructor(label: CreateLabel) : this(label.id, label.name, label.userId, mutableListOf())

    fun add(event: AddFeedToLabel) {
        feeds.add(event.feedId)
    }
}
