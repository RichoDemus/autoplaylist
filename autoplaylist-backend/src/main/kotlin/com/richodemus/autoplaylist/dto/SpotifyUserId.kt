package com.richodemus.autoplaylist.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue

internal data class SpotifyUserId(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "SpotifyUserId can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}
