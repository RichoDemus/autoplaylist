package com.richo.reader.backend.persistence;

import com.google.api.client.util.Sets;
import com.richo.reader.backend.youtube.model.YoutubeChannel;
import com.richo.reader.backend.youtube.model.YoutubeVideo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
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
		final ChannelPersister target = new JsonFileSystemPersistence("target/data/");
		target.updateChannel(new YoutubeChannel("name", Sets.newHashSet()));
		Assert.assertTrue(new File("target/data/").exists());
	}

	@Test
	public void shouldBeAbleToLoadSavedData() throws Exception
	{
		final String saveRoot = "target/data";
		final String channelName = "my-channel";
		final YoutubeVideo firstVideo = new YoutubeVideo("title1", "desc1", "id1", LocalDateTime.now());
		final YoutubeVideo secondVideo = new YoutubeVideo("title2", "desc2", "id2", LocalDateTime.now());
		final YoutubeChannel expected = new YoutubeChannel(channelName, new HashSet<>(Arrays.asList(firstVideo, secondVideo)));
		new JsonFileSystemPersistence(saveRoot).updateChannel(expected);

		final Optional<YoutubeChannel> result = new JsonFileSystemPersistence(saveRoot).getChannel(channelName);

		Assert.assertTrue("Should've gotten a channel", result.isPresent());
		Assert.assertEquals(expected, result.get());

	}
}
