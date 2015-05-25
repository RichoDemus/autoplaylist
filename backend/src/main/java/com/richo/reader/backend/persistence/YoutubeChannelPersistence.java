package com.richo.reader.backend.persistence;

import com.richo.reader.backend.youtube.model.YoutubeChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;

/**
 * Facade for the channel persistence, will look in memory first and then on the filesystem
 */
public class YoutubeChannelPersistence implements ChannelPersister
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final ChannelPersister inMemoryPersister;
	private final ChannelPersister fileSystemPersister;

	@Inject
	public YoutubeChannelPersistence(@Named("InMemory") ChannelPersister inMemoryPersister, @Named("FileSystem") ChannelPersister fileSystemPersister)
	{
		this.inMemoryPersister = inMemoryPersister;
		this.fileSystemPersister = fileSystemPersister;
	}

	@Override
	public Optional<YoutubeChannel> getChannel(String channelName)
	{
		return Optional.ofNullable(inMemoryPersister.getChannel(channelName).orElseGet(() ->
		{
			final Optional<YoutubeChannel> fromFileSystem = fileSystemPersister.getChannel(channelName);
			if (!fromFileSystem.isPresent())
			{
				return null;
			}

			inMemoryPersister.updateChannel(fromFileSystem.get());
			return fromFileSystem.get();
		}));
	}

	@Override
	public void updateChannel(YoutubeChannel channel)
	{
		inMemoryPersister.updateChannel(channel);
		fileSystemPersister.updateChannel(channel);
	}
}
