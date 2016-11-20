package com.richo.reader.web.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemOperation
{
	public static final ItemOperation MARK_AS_READ = new ItemOperation("MARK_READ");
	public static final ItemOperation MARK_AS_UNREAD = new ItemOperation("MARK_UNREAD");
	public static final ItemOperation MARK_OLDER_ITEMS_AS_READ = new ItemOperation("MARK_OLDER_ITEMS_AS_READ");
	private final Operation action;

	@JsonCreator
	public ItemOperation(@JsonProperty("action") String action)
	{
		this.action = Operation.valueOf(action);
	}

	public Operation getAction()
	{
		return action;
	}

	@Override
	public String toString()
	{
		return action.toString();
	}

	public enum Operation
	{
		MARK_READ, MARK_UNREAD, MARK_OLDER_ITEMS_AS_READ
	}
}
