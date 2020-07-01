package com.richodemus.reader.youtube_feed_service

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.FeedName
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Objects

class Feed(val id: FeedId,
           val name: FeedName,
           val items: List<Item>,
           @get:JsonIgnore private val lastUpdated: LocalDateTime) {

    @JsonCreator
    constructor(@JsonProperty("id") id: FeedId,
                @JsonProperty("name") name: FeedName,
                @JsonProperty("items") items: List<Item>,
                @JsonProperty("lastUpdated") lastUpdated: Long) :
            this(id, name, items, LocalDateTime.ofEpochSecond(lastUpdated, 0, ZoneOffset.UTC))

    @JsonProperty("lastUpdated")
    fun getLastUpdatedAsLong() = lastUpdated.toEpochSecond(ZoneOffset.UTC)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val feed = other as Feed?
        return id == feed!!.id && items == feed.items
    }

    override fun hashCode(): Int {
        return Objects.hash(id, items)
    }

    override fun toString(): String {
        return name.toString() + " (" + id + ") " + items.size + " items"
    }
}
