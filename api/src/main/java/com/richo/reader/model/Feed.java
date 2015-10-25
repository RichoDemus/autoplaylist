package com.richo.reader.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

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
}
