package com.richo.reader.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Feed
{
	private final String id;
	private final String name;
	private final List<Item> items;
	private int numberOfAvailableItems;

	@JsonCreator
	public Feed(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("items") List<Item> items)
	{
		this(id, name, items, items.size());
	}

	public Feed(String id, String name, int numberOfAvailableItems)
	{
		this(id, name, new ArrayList<>(), numberOfAvailableItems);
	}

	private Feed(String id, String name, List<Item> items, int numberOfAvailableItems)
	{
		this.id = id;
		this.name = name;
		this.items = items;
		this.numberOfAvailableItems = numberOfAvailableItems;
	}

	public String getId()
	{
		return id;
	}

	public String getName()
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
		if(items.isEmpty())
		{
			return name + ", " + numberOfAvailableItems + " items";
		}
		return name + ", " + items.size() + " items";
	}
}
