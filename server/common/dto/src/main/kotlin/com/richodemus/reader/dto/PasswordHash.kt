package com.richodemus.reader.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue

data class PasswordHash(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "Password can't be empty" }
    }

    @JsonValue override fun toString() = value
}
