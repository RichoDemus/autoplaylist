package com.richo.reader.web.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Feed
{
	private final String id;
	private final String name;
	private final List<Item> items;

	@JsonCreator
	public Feed(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("items") List<Item> items)
	{
		this.id = id;
		this.name = name;
		this.items = items;
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
}
