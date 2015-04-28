package com.richo.reader.web.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemOperation
{
	private final String id;
	private final String action;

	@JsonCreator
	public ItemOperation(@JsonProperty("id") String id, @JsonProperty("action") String action)
	{
		this.id = id;
		this.action = action;
	}

	public String getId()
	{
		return id;
	}

	public String getAction()
	{
		return action;
	}
}
