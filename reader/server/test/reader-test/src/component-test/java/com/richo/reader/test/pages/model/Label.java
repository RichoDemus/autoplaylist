package com.richo.reader.test.pages.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class Label
{
	public final int id;
	public final String name;
	public final List<String> feeds;

	@JsonCreator
	public Label(@JsonProperty("id") final int id, @JsonProperty("name") final String name, @JsonProperty("feeds") final List<String> feeds)
	{
		this.id = id;
		this.name = name;
		this.feeds = feeds;
	}
}
