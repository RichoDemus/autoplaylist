package com.richodemus.autoplaylist.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue

data class AlbumId(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "AlbumId can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}
