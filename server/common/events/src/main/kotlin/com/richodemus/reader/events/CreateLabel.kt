package com.richodemus.reader.events

import com.richodemus.reader.dto.EventId
import com.richodemus.reader.dto.LabelId
import com.richodemus.reader.dto.LabelName
import com.richodemus.reader.dto.UserId

class CreateLabel(eventId: EventId, val labelId: LabelId, val labelName: LabelName, val userId: UserId) : Event(eventId, EventType.CREATE_LABEL) {
    override fun toString() = "Create label $labelName"
}
