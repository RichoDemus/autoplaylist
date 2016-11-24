package com.richo.reader.youtube_feed_service;

import com.richodemus.reader.dto.FeedId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonFileSystemPersistenceTest
{
	private JsonFileSystemPersistence target;

	@Before
	public void setUp() throws Exception
	{
		target = new JsonFileSystemPersistence("target/data/" + UUID.randomUUID());

	}

	@Test
	public void shouldSaveStuffInTheRightPlace() throws Exception
	{
		target.updateChannel(new Feed(new FeedId("name"), new ArrayList<>(), LocalDateTime.ofEpochSecond(0L, 0, ZoneOffset.UTC)));
		Assert.assertTrue(new File("target/data/").exists());
	}

	@Test
	public void shouldBeAbleToLoadSavedData() throws Exception
	{
		final FeedId channelName = new FeedId("my-channel");
		final Item firstVideo = new Item("id1", "title1", "desc1", 0L);
		final Item secondVideo = new Item("id2", "title2", "desc2", 0L);
		final Feed expected = new Feed(channelName, Arrays.asList(firstVideo, secondVideo), LocalDateTime.ofEpochSecond(0L, 0, ZoneOffset.UTC));
		target.updateChannel(expected);

		final Optional<Feed> maybeResult = target.getChannel(channelName);

		Assert.assertTrue("Should've gotten a channel", maybeResult.isPresent());
		final Feed result = maybeResult.get();
		Assert.assertEquals(expected, result);
		assertThat(result.getItems()).isEqualTo(expected.getItems());
	}

	@Test
	public void shouldReturnAllChanelIds() throws Exception
	{
		final List<Feed> feeds = Arrays.asList(new Feed(new FeedId("feed1"), new ArrayList<>(), 0L), new Feed(new FeedId("feed2"), new ArrayList<>(), 0L));

		feeds.forEach(target::updateChannel);

		final List<FeedId> result = target.getAllFeedIds();

		assertThat(result).containsOnly(new FeedId("feed1"), new FeedId("feed2"));
	}
}
