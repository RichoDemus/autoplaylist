package com.richo.reader.web.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedName;

import java.util.Objects;

public class FeedWithoutItems
{
	private final FeedId id;
	private final FeedName name;
	private final int numberOfAvailableItems;

	@JsonCreator
	public FeedWithoutItems(@JsonProperty("id") final FeedId id, @JsonProperty("name") final FeedName name, @JsonProperty("numberOfAvailableItems") final int numberOfAvailableItems)
	{
		this.id = id;
		this.name = name;
		this.numberOfAvailableItems = numberOfAvailableItems;
	}

	public FeedId getId()
	{
		return id;
	}

	public FeedName getName()
	{
		return name;
	}

	public int getNumberOfAvailableItems()
	{
		return numberOfAvailableItems;
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
				.add("id", id)
				.add("name", name)
				.add("numberOfAvailableItems", numberOfAvailableItems)
				.toString();
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
		FeedWithoutItems that = (FeedWithoutItems) o;
		return numberOfAvailableItems == that.numberOfAvailableItems &&
				Objects.equals(id, that.id) &&
				Objects.equals(name, that.name);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id, name, numberOfAvailableItems);
	}
}
