package com.richo.reader.backend.persistence;

import com.richo.reader.backend.youtube.model.YoutubeChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryPersistence implements ChannelPersister
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
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
