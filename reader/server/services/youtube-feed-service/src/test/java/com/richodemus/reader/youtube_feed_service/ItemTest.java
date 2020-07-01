package com.richodemus.reader.youtube_feed_service;

import com.richodemus.reader.dto.ItemId;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemTest {
    @Test
    public void shouldHandleDurationProperly() {
        final Duration duration = Duration.ofMinutes(2);
        final Item item = new Item(new ItemId("id"), "title", "desc", LocalDateTime.now(), LocalDateTime.now(), duration, 1L);
        final Item newItem = new Item(item.getId().getValue(), item.getTitle(), item.getDescription(), item.getUploadDateAsLong(), 0L, item.getDurationAsLong(), item.getViews());

        assertThat(newItem.getDuration()).isEqualTo(newItem.getDuration());
    }
}