package com.richodemus.reader.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue

data class Username(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "Username can't be empty" }
    }

    @JsonValue override fun toString() = value
}