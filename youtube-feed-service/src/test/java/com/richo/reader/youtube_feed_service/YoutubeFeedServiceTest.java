package com.richo.reader.youtube_feed_service;

import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class YoutubeFeedServiceTest
{
	@Test
	public void shouldReturnEmptyOptionalIfChannelIsNotCached() throws Exception
	{
		final Optional<Feed> result = new YoutubeFeedService().getChannel("foo");
		assertThat(result.isPresent()).isFalse();
	}
}