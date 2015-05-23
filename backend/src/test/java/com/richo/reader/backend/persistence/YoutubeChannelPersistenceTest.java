package com.richo.reader.backend.persistence;

import com.google.api.client.util.Sets;
import com.richo.reader.backend.youtube.model.YoutubeChannel;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;

public class YoutubeChannelPersistenceTest
{
	@Test
	public void shouldNotFetchFromFileSystemIfExistsInMemory() throws Exception
	{
		final ChannelPersister fileSystemMock = Mockito.mock(ChannelPersister.class);
		final InMemoryPersistence inMemoryPersister = new InMemoryPersistence();
		inMemoryPersister.updateChannel(new YoutubeChannel("name", Sets.newHashSet()));
		final ChannelPersister target = new YoutubeChannelPersistence(inMemoryPersister, fileSystemMock);
		final Optional<YoutubeChannel> result = target.getChannel("name");

		Assert.assertTrue(result.isPresent());
		Mockito.verifyZeroInteractions(fileSystemMock);
	}

	@Test
	public void shouldFetchFromFileSystemIfNotInMemory() throws Exception
	{
		final ChannelPersister fileSystemMock = Mockito.mock(ChannelPersister.class);
		Mockito.when(fileSystemMock.getChannel("name")).thenReturn(Optional.of(new YoutubeChannel("name", Sets.newHashSet())));

		final ChannelPersister target = new YoutubeChannelPersistence(new InMemoryPersistence(), fileSystemMock);
		final Optional<YoutubeChannel> result = target.getChannel("name");

		Assert.assertTrue(result.isPresent());


	}

	@Test
	public void shouldReturnEmptyOptionalIfNotExistsAtAll() throws Exception
	{
		final ChannelPersister target = new YoutubeChannelPersistence(new InMemoryPersistence(), new InMemoryPersistence());
		Assert.assertFalse(target.getChannel("name").isPresent());

	}
}