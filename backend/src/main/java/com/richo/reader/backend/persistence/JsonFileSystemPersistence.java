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
			return Optional.ofNullable(new ObjectMapper().readValue(new File(saveRoot + "/" + channelName + "/data.json"), YoutubeChannel.class));
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
			final boolean success = new File(saveRoot + "/" + channel.getName()).mkdirs();
			logger.trace("Creating {} successful: {}", saveRoot + "/" + channel.getName(), success);
			new ObjectMapper().writeValue(new File(saveRoot + "/" + channel.getName() + "/data.json"), channel);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
