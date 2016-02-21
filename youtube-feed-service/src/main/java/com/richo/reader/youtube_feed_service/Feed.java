package com.richo.reader.youtube_feed_service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

public class Feed
{
	private final String id;
	private final List<Item> items;
	private final LocalDateTime lastUpdated;

	@JsonCreator
	public Feed(@JsonProperty("id") String id, @JsonProperty("items") List<Item> items, @JsonProperty("lastUpdated") long lastUpdated)
	{
		this.id = id;
		this.items = items;
		this.lastUpdated = LocalDateTime.ofEpochSecond(lastUpdated, 0, ZoneOffset.UTC);
	}

	public Feed(String id, List<Item> items, LocalDateTime lastUpdated)
	{
		this.id = id;
		this.items = items;
		this.lastUpdated = lastUpdated;
	}

	public String getId()
	{
		return id;
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
		return id + " " + items.size() + " items";
	}
}
