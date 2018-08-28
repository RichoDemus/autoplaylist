package com.richodemus.autoplaylist.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue
import java.util.UUID

data class PlaylistId(@get:JsonIgnore val value: UUID = UUID.randomUUID()) {

    @JsonValue
    override fun toString() = value.toString()
}
