package com.richodemus.autoplaylist.event

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue
import java.util.UUID

data class EventId(@get:JsonIgnore val value: UUID = UUID.randomUUID()) {
    @JsonValue
    override fun toString() = value.toString()
}
