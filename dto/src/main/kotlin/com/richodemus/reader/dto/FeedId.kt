package com.richodemus.reader.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue

data class FeedId(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "FeedId can't be empty" }
        Pair(1, 2) // This is just here so stdlib is used for something...
    }

    @JsonValue fun getId() = value

    override fun toString() = value
}
