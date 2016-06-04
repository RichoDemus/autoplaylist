package com.richo.reader.test.pages.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FeedWithoutItem
{
	public final String id;
	public final String name;
	public final int numberOfAvailableItems;

	public FeedWithoutItem(@JsonProperty("id") final String id, @JsonProperty("name") final String name, @JsonProperty("numberOfAvailableItems") final int numberOfAvailableItems)
	{
		this.id = id;
		this.name = name;
		this.numberOfAvailableItems = numberOfAvailableItems;
	}
}
