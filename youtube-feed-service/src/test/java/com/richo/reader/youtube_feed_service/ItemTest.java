package com.richo.reader.youtube_feed_service;

import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemTest
{
	@Test
	public void shouldHandleDurationProperly() throws Exception
	{
		final Duration duration = Duration.ofMinutes(2);
		final Item item = new Item("id", "title", "desc", LocalDateTime.now(), duration, 1L);
		final Item newItem = new Item(item.getId(), item.getTitle(), item.getDescription(), item.getUploadDateAsLong(), item.getDurationAsLong(), item.getViews());

		assertThat(newItem.getDuration()).isEqualTo(newItem.getDuration());
	}
}