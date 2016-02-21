package com.richo.reader.youtube_feed_service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

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
		target.updateChannel(new Feed("name", new ArrayList<>()));
		Assert.assertTrue(new File("target/data/").exists());
	}

	@Test
	public void shouldBeAbleToLoadSavedData() throws Exception
	{
		final String saveRoot = "target/data";
		final String channelName = "my-channel";
		final Item firstVideo = new Item("title1", "desc1", "id1", LocalDateTime.now());
		final Item secondVideo = new Item("title2", "desc2", "id2", LocalDateTime.now());
		final Feed expected = new Feed(channelName, Arrays.asList(firstVideo, secondVideo));
		new JsonFileSystemPersistence(saveRoot).updateChannel(expected);

		final Optional<Feed> result = new JsonFileSystemPersistence(saveRoot).getChannel(channelName);

		Assert.assertTrue("Should've gotten a channel", result.isPresent());
		Assert.assertEquals(expected, result.get());

	}
}
