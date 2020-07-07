package com.richodemus.reader.backend.model;

import com.richodemus.reader.dto.ItemId;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

public class Item {
    private final ItemId id;
    private final String title;
    private final String description;
    private final String uploadDate;
    private final String url;
    private final Duration duration;
    private final long views;

    public Item(ItemId id,
                String title,
                String description,
                String uploadDate,
                String url,
                Duration duration,
                long views) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.uploadDate = uploadDate;
        this.url = url;
        this.duration = duration;
        this.views = views;
    }

    private String toDoubleDigitSeconds(long seconds) {
        final String string = String.valueOf(seconds);
        if (string.length() == 1) {
            return "0" + string;
        }
        return string;
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

    public String getUploadDate() {
        return uploadDate;
    }

    public String getUrl() {
        return url;
    }

    public Duration getDuration() {
        return duration;
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
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public boolean isBefore(Item targetItem) {
        return LocalDate.parse(uploadDate).isBefore(LocalDate.parse(targetItem.getUploadDate()));
    }
}
