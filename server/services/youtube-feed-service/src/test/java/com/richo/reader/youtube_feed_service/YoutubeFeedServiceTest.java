package com.richo.reader.youtube_feed_service;

import com.codahale.metrics.MetricRegistry;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedName;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class YoutubeFeedServiceTest
{
	private static final FeedId NON_CACHED_CHANNEL = new FeedId("foo");
	private static final Feed CACHED_CHANNEL = new Feed(new FeedId("RichoDemus"), new FeedName("richodemus"), new ArrayList<>(), LocalDateTime.ofEpochSecond(0L, 0, ZoneOffset.UTC));
	private static final Feed CHANNEL_ON_DISK = new Feed(new FeedId("Ylvis"), new FeedName("ylvis"), new ArrayList<>(), LocalDateTime.ofEpochSecond(0L, 0, ZoneOffset.UTC));
	private YoutubeFeedService target;

	@Before
	public void setUp() throws Exception
	{
		final JsonFileSystemPersistence fileSystemPersistence = new JsonFileSystemPersistence("target/data/" + UUID.randomUUID());
		fileSystemPersistence.updateChannel(CHANNEL_ON_DISK);
		final FeedCache cache = new FeedCache(fileSystemPersistence);
		cache.update(CACHED_CHANNEL);
		target = new YoutubeFeedService(cache, null, new MetricRegistry());
	}

	@Test
	public void shouldReturnedFeedIfItsCached() throws Exception
	{
		final Optional<Feed> result = target.getChannel(CACHED_CHANNEL.getId());
		assertThat(result.isPresent()).isTrue();
	}

	@Test
	public void shouldReturnFeedIfItsWrittenToDisk() throws Exception
	{
		final Optional<Feed> result = target.getChannel(CHANNEL_ON_DISK.getId());
		assertThat(result.isPresent()).isTrue();
	}

	@Test
	public void shouldCacheFeedsReadFromDisk() throws Exception
	{
		final JsonFileSystemPersistence fileSystemPersistence = mock(JsonFileSystemPersistence.class);
		when(fileSystemPersistence.getChannel(CHANNEL_ON_DISK.getId())).thenReturn(Optional.of(CHANNEL_ON_DISK));
		final FeedCache cache = new FeedCache(fileSystemPersistence);
		cache.update(CACHED_CHANNEL);
		target = new YoutubeFeedService(cache, null, new MetricRegistry());

		target.getChannel(CHANNEL_ON_DISK.getId());
		target.getChannel(CHANNEL_ON_DISK.getId());

		verify(fileSystemPersistence, times(1)).getChannel(CHANNEL_ON_DISK.getId());
	}

	@Test
	public void shouldReturnEmptyOptionalIfChannelIsNotCached() throws Exception
	{
		final Optional<Feed> result = target.getChannel(NON_CACHED_CHANNEL);
		assertThat(result.isPresent()).isFalse();
	}
}
