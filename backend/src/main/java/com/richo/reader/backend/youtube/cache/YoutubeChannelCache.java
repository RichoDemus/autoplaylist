package com.richo.reader.backend.youtube.cache;

import com.richo.reader.backend.youtube.model.YoutubeChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class YoutubeChannelCache
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Map<String, YoutubeChannel> channels;

	public YoutubeChannelCache()
	{
		channels = new HashMap<>();
	}

	public Optional<YoutubeChannel> getChannel(String channelName)
	{
		return Optional.ofNullable(channels.get(channelName));
	}

	public void updateChannel(YoutubeChannel channel)
	{
		channels.put(channel.getName(), channel);
	}
}
