package com.richo.reader.youtube_feed_service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonFileSystemPersistenceTest
{
	@Before
	public void setUp() throws Exception
	{
		new File("target/data").delete();
	}

	@Test
	public void shouldSaveStuffInTheRightPlace() throws Exception
	{
		final JsonFileSystemPersistence target = new JsonFileSystemPersistence("target/data/");
		target.updateChannel(new Feed("name", new ArrayList<>(), LocalDateTime.ofEpochSecond(0L, 0, ZoneOffset.UTC)));
		Assert.assertTrue(new File("target/data/").exists());
	}

	@Test
	public void shouldBeAbleToLoadSavedData() throws Exception
	{
		final String saveRoot = "target/data";
		final String channelName = "my-channel";
		final Item firstVideo = new Item("id1", "title1", "desc1", 0L);
		final Item secondVideo = new Item("id2", "title2", "desc2", 0L);
		final Feed expected = new Feed(channelName, Arrays.asList(firstVideo, secondVideo), LocalDateTime.ofEpochSecond(0L, 0, ZoneOffset.UTC));
		new JsonFileSystemPersistence(saveRoot).updateChannel(expected);

		final Optional<Feed> maybeResult = new JsonFileSystemPersistence(saveRoot).getChannel(channelName);

		Assert.assertTrue("Should've gotten a channel", maybeResult.isPresent());
		final Feed result = maybeResult.get();
		Assert.assertEquals(expected, result);
		assertThat(result.getItems()).isEqualTo(expected.getItems());
	}
}
