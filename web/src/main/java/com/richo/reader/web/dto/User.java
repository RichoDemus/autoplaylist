package com.richo.reader.web.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.richo.reader.backend.model.Feed;
import com.richo.reader.backend.model.Label;

import java.util.List;

public class User
{
	private final List<Feed> feeds;
	private final List<Label> labels;

	@JsonCreator
	public User(@JsonProperty("feeds") List<Feed> feeds, @JsonProperty("labels") List<Label> labels)
	{
		this.feeds = feeds;
		this.labels = labels;
	}

	public List<Feed> getFeeds()
	{
		return feeds;
	}

	public List<Label> getLabels()
	{
		return labels;
	}
}
