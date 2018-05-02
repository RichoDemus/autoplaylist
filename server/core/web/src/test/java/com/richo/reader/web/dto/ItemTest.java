package com.richo.reader.web.dto;

import com.richodemus.reader.dto.ItemId;
import com.richodemus.reader.web.dto.Item;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.time.Duration;

public class ItemTest
{
	@Test
	public void shouldDisplayTimeCorrectly() {
		assertDuration(Duration.ofHours(2), "120:00");
		assertDuration(Duration.ofHours(1), "60:00");
		assertDuration(Duration.ofMinutes(1), "1:00");
		assertDuration(Duration.ofSeconds(1), "0:01");
		assertDuration(Duration.ofSeconds(65), "1:05");
		assertDuration(Duration.ofSeconds(75), "1:15");
	}

	private void assertDuration(Duration duration, String expected)
	{
		Assertions.assertThat(new Item(new ItemId("id"), "title", "desc", "date", "url", duration, 0L).getDuration()).isEqualTo(expected);
	}
}
