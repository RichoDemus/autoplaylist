package com.richo.reader.backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class Label
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final long id;
	private final String name;
	private List<String> feeds;

	@JsonCreator
	public Label(@JsonProperty("id") long id, @JsonProperty("name") String name, @JsonProperty("feeds") List<String> feeds)
	{
		this.id = id;
		this.name = name;
		this.feeds = feeds;
	}

	public Label(long id, String name)
	{
		this(id, name, Collections.emptyList());
	}

	public long getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public void addFeed(String feedId)
	{
		feeds.add(feedId);
	}

	public List<String> getFeeds()
	{
		return feeds;
	}
}
