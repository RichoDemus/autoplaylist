package com.richodemus.reader.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue

data class ItemId(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "ItemId can't be empty" }
    }

    @JsonValue override fun toString() = value
}
