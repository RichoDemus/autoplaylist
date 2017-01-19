package com.richo.reader.web.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.richo.reader.backend.model.FeedWithoutItems;
import com.richo.reader.backend.model.Label;

import java.util.List;

public class User
{
	private final List<FeedWithoutItems> feeds;
	private final List<Label> labels;

	@JsonCreator
	public User(@JsonProperty("feeds") List<FeedWithoutItems> feeds, @JsonProperty("labels") List<Label> labels)
	{
		this.feeds = feeds;
		this.labels = labels;
	}

	public List<FeedWithoutItems> getFeeds()
	{
		return feeds;
	}

	public List<Label> getLabels()
	{
		return labels;
	}
}
