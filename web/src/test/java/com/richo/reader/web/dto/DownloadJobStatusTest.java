package com.richo.reader.web.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.richodemus.reader.web.dto.DownloadJobStatus;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

public class DownloadJobStatusTest
{
	@Test
	public void shouldSerializeIntoCorrectJson() throws Exception
	{
		final String result = new ObjectMapper().writeValueAsString(new DownloadJobStatus(LocalDateTime.ofEpochSecond(1000L, 0, ZoneOffset.UTC), false));

		//language=JSON
		final String expected = "{\"lastRun\":\"1970-01-01T00:16:40\",\"running\":false}";

		assertThat(result).isEqualTo(expected);
	}
}