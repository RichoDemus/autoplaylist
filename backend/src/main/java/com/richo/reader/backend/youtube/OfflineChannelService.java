package com.richo.reader.backend.youtube;

import com.richo.reader.backend.persistence.YoutubeChannelPersistence;
import com.richo.reader.backend.youtube.model.YoutubeChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;

public class OfflineChannelService extends YoutubeChannelService
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final YoutubeChannelPersistence cache;

	@Inject
	public OfflineChannelService(YoutubeChannelPersistence cache)
	{
		super(null, cache, null);
		this.cache = cache;
	}

	@Override
	public Optional<YoutubeChannel> getChannelByName(String channelName)
	{
		return cache.getChannel(channelName);
	}
}
