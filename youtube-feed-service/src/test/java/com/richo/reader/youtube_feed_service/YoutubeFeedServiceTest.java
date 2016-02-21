package com.richo.reader.youtube_feed_service;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class YoutubeFeedServiceTest
{
	private static final String NON_CACHED_CHANNEL = "foo";
	private static final Feed CACHED_CHANNEL = new Feed("RichoDemus");
	private YoutubeFeedService target;

	@Before
	public void setUp() throws Exception
	{
		final FeedCache cache = new FeedCache();
		cache.add(CACHED_CHANNEL);
		target = new YoutubeFeedService(cache);
	}

	@Test
	public void shouldReturnedFeedIfItsCached() throws Exception
	{
		final Optional<Feed> result = target.getChannel(CACHED_CHANNEL.getId());
		assertThat(result.isPresent()).isTrue();
	}

	@Test
	public void shouldReturnEmptyOptionalIfChannelIsNotCached() throws Exception
	{
		final Optional<Feed> result = target.getChannel(NON_CACHED_CHANNEL);
		assertThat(result.isPresent()).isFalse();
	}
}
