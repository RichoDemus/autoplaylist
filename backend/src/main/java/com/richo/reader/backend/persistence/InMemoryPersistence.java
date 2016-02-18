package com.richo.reader.backend.persistence;

import com.richo.reader.backend.youtube.model.YoutubeChannel;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class InMemoryPersistence implements ChannelPersister
{
	private final Map<String, YoutubeChannel> channels;

	public InMemoryPersistence()
	{
		channels = new HashMap<>();
	}

	@Override
	public Optional<YoutubeChannel> getChannel(String channelName)
	{
		return Optional.ofNullable(channels.get(channelName));
	}

	@Override
	public void updateChannel(YoutubeChannel channel)
	{
		channels.put(channel.getName(), channel);
	}
}
