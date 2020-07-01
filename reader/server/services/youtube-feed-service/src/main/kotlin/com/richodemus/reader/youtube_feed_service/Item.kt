package com.richodemus.reader.youtube_feed_service

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.richodemus.reader.dto.ItemId
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

class Item(val id: ItemId,
           val title: String,
           val description: String,
           @get:JsonIgnore
           val uploadDate: LocalDateTime,
           /**
            * When the application first found this Item
            */
           @get:JsonIgnore
           val added: LocalDateTime,
           @get:JsonIgnore
           val duration: Duration,
           val views: Long) {

    @JsonCreator
    constructor(@JsonProperty("id") id: String,
                @JsonProperty("title") title: String,
                @JsonProperty("description") description: String,
                @JsonProperty("uploadDate") uploadDate: Long,
                @JsonProperty("added") added: Long,
                @JsonProperty("duration") duration: Long,
                @JsonProperty("views") views: Long) :
            this(ItemId(id),
                    title,
                    description,
                    LocalDateTime.ofEpochSecond(uploadDate, 0, ZoneOffset.UTC),
                    LocalDateTime.ofEpochSecond(added, 0, ZoneOffset.UTC),
                    Duration.ofSeconds(duration),
                    views)

    @JsonProperty("uploadDate")
    fun getUploadDateAsLong() = uploadDate.toEpochSecond(ZoneOffset.UTC)

    @JsonProperty("added")
    fun getAddedAsLong() = added.toEpochSecond(ZoneOffset.UTC)

    @JsonProperty("duration")
    fun getDurationAsLong() = duration.seconds

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val item = other as Item?
        return id == item!!.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return title
    }
}
