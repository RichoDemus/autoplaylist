package com.richodemus.reader.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue
import java.util.UUID

data class LabelId(@get:JsonIgnore val value: UUID) {
    constructor() : this(UUID.randomUUID())

    @JsonValue
    override fun toString() = value.toString()
}
