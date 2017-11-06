package com.richodemus.reader.events

import com.richodemus.reader.dto.LabelId
import com.richodemus.reader.dto.LabelName
import com.richodemus.reader.dto.UserId

class CreateLabel(val labelId: LabelId, val labelName: LabelName, val userId: UserId) : Event(type = EventType.CREATE_LABEL) {
    override fun toString() = "Create label $labelName"
}
