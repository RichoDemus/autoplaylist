package com.richodemus.reader.label_service

import com.richodemus.reader.dto.LabelId
import com.richodemus.reader.dto.LabelName
import com.richodemus.reader.dto.UserId

data class User(val id: UserId) {
    private val labels = mutableListOf<Label>()
    fun createLabel(id: LabelId, name: LabelName) {
//        labels.add(Label(id, name))
    }
}
