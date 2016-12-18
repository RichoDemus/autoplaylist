package com.richodemus.reader.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue

class UserId(value: String) {
    @get:JsonIgnore val value: String

    init {
        require(value.isNotBlank()) { "UserId can't be empty" }
        this.value = value.toLowerCase()
    }

    @JsonValue override fun toString() = value

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as UserId

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}
