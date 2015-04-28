package com.richo.reader.web.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FeedResult
{
	private final List<Feed> feeds;

	@JsonCreator
	public FeedResult(@JsonProperty("feeds") List<Feed> feeds)
	{
		this.feeds = feeds;
	}

	public List<Feed> getFeeds()
	{
		return feeds;
	}
}
