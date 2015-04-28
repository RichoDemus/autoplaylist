package com.richo.reader.web.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Feed
{
	private final String name;
	private final List<Item> items;

	@JsonCreator
	public Feed(@JsonProperty("name") String name, @JsonProperty("items") List<Item> items)
	{
		this.name = name;
		this.items = items;
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
