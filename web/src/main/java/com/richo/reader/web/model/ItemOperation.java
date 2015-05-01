package com.richo.reader.web.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemOperation
{
	private final String action;

	@JsonCreator
	public ItemOperation(@JsonProperty("action") String action)
	{
		this.action = action;
	}

	public String getAction()
	{
		return action;
	}
}
