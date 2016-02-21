package com.richo.reader.youtube_feed_service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class Feed
{
	private final String id;
	private final List<Item> items;

	@JsonCreator
	public Feed(@JsonProperty("id") String id, @JsonProperty("items") List<Item> items)
	{

		this.id = id;
		this.items = items;
	}

	public String getId()
	{
		return id;
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
