package com.richodemus.reader.dto

import com.fasterxml.jackson.annotation.JsonValue

data class ItemId(val value: String) {
    init {
        require(value.isNotBlank()) { "FeedId can't be empty" }
    }

    @JsonValue fun getId() = value

    override fun toString() = value
}
