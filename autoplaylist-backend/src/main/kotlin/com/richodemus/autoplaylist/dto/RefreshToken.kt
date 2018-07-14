package com.richodemus.autoplaylist.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue

internal data class RefreshToken(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "AccessToken can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}
