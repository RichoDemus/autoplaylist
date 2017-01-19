package com.richo.reader.youtube_feed_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.richodemus.reader.dto.FeedId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;


public class JsonFileSystemPersistence
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final String saveRoot;
	private ObjectMapper objectMapper;

	/**
	 * Just used to create some test data
	 */
	public static void main(String[] args)
	{
		new JsonFileSystemPersistence("data/").updateChannel(
				new Feed(new FeedId("richodemus"),
						singletonList(new Item("id", "title", "desc", 0L, 0L, 60L, 100L)),
						0L));
	}

	@Inject
	public JsonFileSystemPersistence(@Named("saveRoot") String saveRoot)
	{
		this.saveRoot = saveRoot;
		this.objectMapper = new ObjectMapper();
	}

	public Optional<Feed> getChannel(FeedId feedId)
	{
		try
		{
			final File file = new File(saveRoot + "/feeds/" + feedId + "/data.json");
			if (!file.exists())
			{
				logger.debug("Feed {} not on disk", feedId);
				return Optional.empty();
			}
			logger.trace("Reading feed {} from disk", feedId);
			return Optional.ofNullable(objectMapper.readValue(file, Feed.class));
		}
		catch (Exception e)
		{
			logger.warn("Unable to load feed: {}", feedId, e);
			return Optional.empty();
		}
	}

	void updateChannel(final Feed feed)
	{
		try
		{
			feed.getItems().sort(Comparator.comparing(Item::getUploadDate));

			final String path = saveRoot + "/feeds/" + feed.getId();
			final boolean success = new File(path).mkdirs();
			logger.trace("Creating {} successful: {}", path, success);
			objectMapper.writeValue(new File(path + "/data.json"), feed);
		}
		catch (IOException e)
		{
			logger.warn("Unable to write feed {} to disk", feed.getId(), e);
		}
	}

	List<FeedId> getAllFeedIds()
	{
		final File[] directories = new File(saveRoot + "/feeds/").listFiles(File::isDirectory);
		if (directories == null)
		{
			return new ArrayList<>();
		}
		return Arrays.stream(directories)
				.map(File::getName)
				.map(FeedId::new)
				.collect(Collectors.toList());
	}
}
