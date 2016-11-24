package com.richo.reader.backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.richodemus.reader.dto.FeedId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Feed
{
	private final FeedId id;
	private final FeedId name;
	private final List<Item> items;
	private int numberOfAvailableItems;

	@JsonCreator
	public Feed(@JsonProperty("id") FeedId id, @JsonProperty("name") FeedId name, @JsonProperty("items") List<Item> items)
	{
		this(id, name, items, items.size());
	}

	public Feed(FeedId id, FeedId name, int numberOfAvailableItems)
	{
		this(id, name, new ArrayList<>(), numberOfAvailableItems);
	}

	private Feed(FeedId id, FeedId name, List<Item> items, int numberOfAvailableItems)
	{
		this.id = id;
		this.name = name;
		this.items = items;
		this.numberOfAvailableItems = numberOfAvailableItems;
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

	public int getNumberOfAvailableItems()
	{
		return numberOfAvailableItems;
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
		return numberOfAvailableItems == feed.numberOfAvailableItems &&
				Objects.equals(id, feed.id) &&
				Objects.equals(name, feed.name) &&
				Objects.equals(items, feed.items);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id, name, items, numberOfAvailableItems);
	}

	@Override
	public String toString()
	{
		if (items.isEmpty())
		{
			return name + ", " + numberOfAvailableItems + " items";
		}
		return name + ", " + items.size() + " items";
	}
}
