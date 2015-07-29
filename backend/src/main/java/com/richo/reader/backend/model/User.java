package com.richo.reader.backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.richo.reader.backend.exception.UserNotSubscribedToThatChannelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class User
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final String name;
	private final Map<String, Set<String>> feeds;

	@JsonCreator
	public User(@JsonProperty("name") String username, @JsonProperty("feeds") Map<String, Set<String>> feedsIds)
	{
		this.name = username;
		this.feeds = feedsIds;
	}

	public User(String username, Set<String> feedIds)
	{
		this.name = username;
		this.feeds = new HashMap<>();
		feedIds.forEach(id -> feeds.put(id, new HashSet<>()));
	}

	public Map<String, Set<String>> getFeeds()
	{
		return ImmutableMap.copyOf(feeds);
	}

	public void addFeed(String feedId)
	{
		feeds.put(feedId, new HashSet<>());
	}

	public String getName()
	{
		return name;
	}

	public void markAsRead(String feedId, String itemId) throws UserNotSubscribedToThatChannelException
	{
		if(!feeds.containsKey(feedId))
		{
			throw new UserNotSubscribedToThatChannelException(name + " is not subscribed to feed " + feedId);
		}

		feeds.get(feedId).add(itemId);
	}

	public boolean isRead(String feedId, String videoId)
	{
		return feeds.get(feedId).contains(videoId);
	}
}
