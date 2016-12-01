package com.richo.reader.backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.richodemus.reader.dto.FeedId;

import java.util.List;

public class Label
{
	private final long id;
	private final String name;
	private final List<FeedId> feeds;

	@JsonCreator
	public Label(@JsonProperty("id") long id, @JsonProperty("name") String name, @JsonProperty("feeds") List<FeedId> feeds)
	{
		this.id = id;
		this.name = name;
		this.feeds = feeds;
	}

	public long getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public List<FeedId> getFeeds()
	{
		return feeds;
	}
}
