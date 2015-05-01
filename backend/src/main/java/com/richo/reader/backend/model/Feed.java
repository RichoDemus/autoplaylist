package com.richo.reader.backend.model;

import com.google.api.client.util.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class Feed
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final String id;
	private final String name;
	private final Set<Item> items;
	private final Set<String> readItems;

	public Feed(String id, String name, Set<Item> items, Set<String> readItems)
	{
		this.id = id;
		this.name = name;
		this.items = items;
		this.readItems = readItems;
	}

	public Feed(String id, String name)
	{
		this.id = id;
		this.name = name;
		this.items = new HashSet<>();
		readItems = Sets.newHashSet();
	}

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public void addNewItems(Set<Item> items)
	{
		items.stream().filter(this::filterUnreadItems).forEach(this.items::add);
	}

	private boolean filterUnreadItems(Item item)
	{
		return !readItems.contains(item.getVideoId());
	}

	public Set<Item> getItems()
	{
		return items;
	}

	public void markAsRead(String id)
	{
		readItems.add(id);
	}
}
