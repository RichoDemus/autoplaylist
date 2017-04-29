package com.richodemus.reader.dto

// todo shouldn't be here
class Label(val id: LabelId, val name: LabelName, val feeds: List<FeedId>) {
    constructor(id: LabelId, name: LabelName) : this(id, name, emptyList())
}
