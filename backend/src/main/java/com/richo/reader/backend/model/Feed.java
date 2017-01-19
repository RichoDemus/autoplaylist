package com.richo.reader.backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.richodemus.reader.dto.FeedId;

import java.util.List;
import java.util.Objects;

public class Feed
{
	private final FeedId id;
	private final FeedId name;
	private final List<Item> items;

	@JsonCreator
	public Feed(@JsonProperty("id") FeedId id, @JsonProperty("name") FeedId name, @JsonProperty("items") List<Item> items)
	{
		this.id = id;
		this.name = name;
		this.items = items;
	}

	public FeedId getId()
	{
		return id;
	}

	public FeedId getName()
	{
		return name;
	}

	public List<Item> getItems()
	{
		return items;
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
				Objects.equals(name, feed.name) &&
				Objects.equals(items, feed.items);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id, name, items);
	}

	@Override
	public String toString()
	{
		return name + ", " + items.size() + " items";
	}
}
