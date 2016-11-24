package com.richodemus.reader.dto

import com.fasterxml.jackson.annotation.JsonValue

data class FeedId(val value: String) {
    init {
        if (value.isEmpty()) {
            Pair(1,2)
            throw IllegalArgumentException("FeedId can't be empty")
        }
    }

    @JsonValue fun getId() = value

    override fun toString() = value
}
