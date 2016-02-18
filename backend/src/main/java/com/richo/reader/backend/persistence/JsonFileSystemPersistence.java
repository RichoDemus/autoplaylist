package com.richo.reader.backend.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.richo.reader.backend.youtube.model.YoutubeChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.util.Optional;


public class JsonFileSystemPersistence implements ChannelPersister
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final String saveRoot;

	@Inject
	public JsonFileSystemPersistence(@Named("saveRoot") String saveRoot)
	{
		this.saveRoot = saveRoot;
	}

	@Override
	public Optional<YoutubeChannel> getChannel(String channelName)
	{
		try
		{
			final File file = new File(saveRoot + "/channels/" + channelName + "/data.json");
			if(!file.exists())
			{
				logger.debug("Channel {} not on disk", channelName);
				return Optional.empty();
			}
			logger.trace("Reading channel {} from disk", channelName);
			return Optional.ofNullable(new ObjectMapper().readValue(file, YoutubeChannel.class));
		}
		catch (IOException e)
		{
			logger.debug("Unable to load channel: {}", channelName, e);
			return Optional.empty();
		}
	}

	@Override
	public void updateChannel(YoutubeChannel channel)
	{
		try
		{
			final String path = saveRoot + "/channels/" + channel.getName();
			final boolean success = new File(path).mkdirs();
			logger.trace("Creating {} successful: {}", path, success);
			new ObjectMapper().writeValue(new File(path + "/data.json"), channel);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
