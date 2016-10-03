package com.richo.reader.backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.richo.reader.backend.exception.UserNotSubscribedToThatChannelException;
import com.richo.reader.model.Label;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class User
{
	private final String name;
	private final Map<String, Set<String>> feeds;
	private final List<Label> labels;
	private long nextLabelId;

	@JsonCreator
	public User(@JsonProperty("name") String username, @JsonProperty("nextLabelId") long nextLabelId, @JsonProperty("feeds") Map<String, Set<String>> feedsIds, @JsonProperty("labels") List<Label> labels)
	{
		this.name = username;
		this.nextLabelId = nextLabelId;
		this.feeds = feedsIds;
		this.labels = labels;
	}

	public User(String username, Set<String> feedIds)
	{
		this.name = username;
		this.feeds = new HashMap<>();
		this.labels = new ArrayList<>();
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
		if (!feeds.containsKey(feedId))
		{
			throw new UserNotSubscribedToThatChannelException(name + " is not subscribed to feed " + feedId);
		}

		feeds.get(feedId).add(itemId);
	}

	public boolean isRead(String feedId, String videoId)
	{
		return feeds.get(feedId).contains(videoId);
	}

	public void markAsUnRead(String feedId, String itemId)
	{
		feeds.get(feedId).remove(itemId);
	}

	public long getNextLabelId()
	{
		return nextLabelId;
	}

	@JsonIgnore
	public synchronized long incrementAndGetNextLabelId()
	{
		return nextLabelId++;
	}

	public List<Label> getLabels()
	{
		return labels;
	}

	@JsonIgnore
	public void addLabel(Label label)
	{
		labels.add(label);
	}

	@JsonIgnore
	@Override
	public String toString()
	{
		return name;
	}
}
