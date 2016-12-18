package com.richo.reader.backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.richo.reader.backend.exception.UserNotSubscribedToThatChannelException;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.ItemId;
import com.richodemus.reader.dto.UserId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class User
{
	private final UserId name;
	private final Map<FeedId, Set<ItemId>> feeds;
	private final List<Label> labels;
	private long nextLabelId;

	@JsonCreator
	public User(@JsonProperty("name") UserId username, @JsonProperty("nextLabelId") long nextLabelId, @JsonProperty("feeds") Map<FeedId, Set<ItemId>> feedsIds, @JsonProperty("labels") List<Label> labels)
	{
		this.name = username;
		this.nextLabelId = nextLabelId;
		this.feeds = feedsIds;
		this.labels = labels;
	}

	public User(UserId username, Set<FeedId> feedIds)
	{
		this.name = username;
		this.feeds = new HashMap<>();
		this.labels = new ArrayList<>();
		feedIds.forEach(id -> feeds.put(id, new HashSet<>()));
	}

	public Map<FeedId, Set<ItemId>> getFeeds()
	{
		return ImmutableMap.copyOf(feeds);
	}

	public void addFeed(FeedId feedId)
	{
		feeds.put(feedId, new HashSet<>());
	}

	public UserId getName()
	{
		return name;
	}

	public void markAsRead(FeedId feedId, ItemId itemId) throws UserNotSubscribedToThatChannelException
	{
		if (!feeds.containsKey(feedId))
		{
			throw new UserNotSubscribedToThatChannelException(name + " is not subscribed to feed " + feedId);
		}

		feeds.get(feedId).add(itemId);
	}

	public boolean isRead(FeedId feedId, ItemId videoId)
	{
		return feeds.get(feedId).contains(videoId);
	}

	public void markAsUnRead(FeedId feedId, ItemId itemId)
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
		return name.toString();
	}
}
