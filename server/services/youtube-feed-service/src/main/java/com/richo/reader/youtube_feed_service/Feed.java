package com.richo.reader.youtube_feed_service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedName;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class Feed
{
	private final FeedId id;
	private final FeedName name;
	private final List<Item> items;
	private final LocalDateTime lastUpdated;

	@JsonCreator
	public Feed(@JsonProperty("id") FeedId id,
				@JsonProperty("name") FeedName name,
				@JsonProperty("items") List<Item> items,
				@JsonProperty("lastUpdated") long lastUpdated)
	{
		this(id, name, items, LocalDateTime.ofEpochSecond(lastUpdated, 0, ZoneOffset.UTC));
	}

	public Feed(FeedId id, FeedName name, List<Item> items, LocalDateTime lastUpdated)
	{
		this.id = checkNotNull(id, "Id can't be null");
		this.name = name;
		this.items = items;
		this.lastUpdated = lastUpdated;
	}

	public FeedId getId()
	{
		return id;
	}

	public FeedName getName()
	{
		return name;
	}

	public List<Item> getItems()
	{
		return items;
	}

	@JsonIgnore
	public LocalDateTime getLastUpdated()
	{
		return lastUpdated;
	}

	@JsonProperty("lastUpdated")
	public long getLastUpdatedAsLong()
	{
		return lastUpdated.toEpochSecond(ZoneOffset.UTC);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		Feed feed = (Feed) o;
		return Objects.equals(id, feed.id) &&
				Objects.equals(items, feed.items);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id, items);
	}

	@Override
	public String toString()
	{
		return name + " (" + id + ") " + items.size() + " items";
	}
}
