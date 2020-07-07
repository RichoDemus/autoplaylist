package com.richodemus.reader.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue
import java.net.URL

class FeedUrl(url: String) {
    @get:JsonIgnore
    val value = URL(url)

    @JsonValue
    override fun toString() = value.toString()

    override fun equals(other: Any?): Boolean {
        if (other is FeedUrl) {
            return value == other.value
        }
        return false
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}
