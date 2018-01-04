package com.richo.reader.youtube_feed_service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.richodemus.reader.dto.ItemId;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Item {
    private final ItemId id;
    private final String title;
    private final String description;
    private final LocalDateTime uploadDate;
    /**
     * When the application first found this Item
     */
    private final LocalDateTime added;
    private final Duration duration;
    private final long views;

    @JsonCreator
    public Item(@JsonProperty("id") String id,
                @JsonProperty("title") String title,
                @JsonProperty("description") String description,
                @JsonProperty("uploadDate") long uploadDate,
                @JsonProperty("added") long added,
                @JsonProperty("duration") long duration,
                @JsonProperty("views") long views) {
        this(new ItemId(id),
                title,
                description,
                LocalDateTime.ofEpochSecond(uploadDate, 0, ZoneOffset.UTC),
                LocalDateTime.ofEpochSecond(added, 0, ZoneOffset.UTC),
                Duration.ofSeconds(duration),
                views);
    }

    public Item(ItemId id,
                String title,
                String description,
                LocalDateTime uploadDate,
                LocalDateTime added,
                Duration duration,
                long views) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.uploadDate = uploadDate;
        this.added = added;
        this.duration = duration;
        this.views = views;
    }

    public ItemId getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    @JsonIgnore
    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    @JsonProperty("uploadDate")
    public long getUploadDateAsLong() {
        return uploadDate.toEpochSecond(ZoneOffset.UTC);
    }

    @JsonIgnore
    public LocalDateTime getAdded() {
        return added;
    }

    @JsonProperty("added")
    public long getAddedAsLong() {
        return added.toEpochSecond(ZoneOffset.UTC);
    }

    @JsonIgnore
    public Duration getDuration() {
        return duration;
    }

    @JsonProperty("duration")
    public long getDurationAsLong() {
        return duration.getSeconds();
    }

    public long getViews() {
        return views;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Item item = (Item) o;
        return id.equals(item.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return title;
    }
}
