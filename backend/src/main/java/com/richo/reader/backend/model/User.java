package com.richo.reader.backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class User
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final String name;
	private final Set<String> feedsIds;

	@JsonCreator
	public User(@JsonProperty("name") String username, @JsonProperty("feeds") Set<String> feedsIds)
	{
		this.name = username;
		this.feedsIds = feedsIds;
	}

	public Set<String> getFeeds()
	{
		return feedsIds;
	}

	public void addFeed(String feedId)
	{
		feedsIds.add(feedId);
	}

	public String getName()
	{
		return name;
	}
}
