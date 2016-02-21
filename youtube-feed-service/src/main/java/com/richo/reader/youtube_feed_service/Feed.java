package com.richo.reader.youtube_feed_service;

import java.util.List;

public class Feed
{
	private final String id;
	private final List<Item> items;

	public Feed(String id, List<Item> items)
	{

		this.id = id;
		this.items = items;
	}

	public String getId()
	{
		return id;
	}
}
