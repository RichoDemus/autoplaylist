package com.richodemus.reader.reader.test.pages.model

class FeedUrl(private val value: String) {
    fun toJson(): String {
        return "\"" + value + "\""
    }

    override fun toString(): String {
        return value
    }
}
