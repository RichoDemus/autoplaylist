package com.richo.reader.youtube_feed_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class JsonFileSystemPersistence
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final String saveRoot;

	@Inject
	public JsonFileSystemPersistence(@Named("saveRoot") String saveRoot)
	{
		this.saveRoot = saveRoot;
	}

	public Optional<Feed> getChannel(String feedId)
	{
		try
		{
			final File file = new File(saveRoot + "/feeds/" + feedId + "/data.json");
			if(!file.exists())
			{
				logger.debug("Feed {} not on disk", feedId);
				return Optional.empty();
			}
			logger.trace("Reading feed {} from disk", feedId);
			return Optional.ofNullable(new ObjectMapper().readValue(file, Feed.class));
		}
		catch (Exception e)
		{
			logger.warn("Unable to load feed: {}", feedId, e);
			return Optional.empty();
		}
	}

	void updateChannel(Feed feed)
	{
		try
		{
			final String path = saveRoot + "/feeds/" + feed.getId();
			final boolean success = new File(path).mkdirs();
			logger.trace("Creating {} successful: {}", path, success);
			new ObjectMapper().writeValue(new File(path + "/data.json"), feed);
		}
		catch (IOException e)
		{
			logger.warn("Unable to write feed {} to disk", feed.getId(), e);
		}
	}

	public List<String> getAllFeedIds()
	{
		final File[] directories = new File(saveRoot + "/feeds/").listFiles(File::isDirectory);
		if (directories == null)
		{
			return new ArrayList<>();
		}
		return Arrays.asList(directories).stream()
				.map(File::getName)
				.collect(Collectors.toList());
	}
}
