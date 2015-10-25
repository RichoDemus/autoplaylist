package com.richo.reader.web;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.richo.reader.backend.model.Feed;
import com.richo.reader.backend.model.Item;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Set;

public class TestData
{
	public static final Item FEED1_ITEM1 = createItem("id11", "title11", "desc11");
	public static final Item FEED1_ITEM2 = createItem("id12", "title12", "desc12");
	public static final Feed FEED1 = new Feed("id1", "name1", Sets.newHashSet(FEED1_ITEM1, FEED1_ITEM2), Sets.newHashSet());

	public static final Item FEED2_ITEM1 = createItem("id21", "title21", "desc21");
	public static final Item FEED2_ITEM2 = createItem("id22", "title22", "desc22");
	public static final Feed FEED2 = new Feed("id2", "name2", Sets.newHashSet(FEED2_ITEM1, FEED2_ITEM2), Sets.newHashSet());

	public static final Set<Feed> FEEDS = ImmutableSet.of(FEED1, FEED2);

	private static Item createItem(String id, String title, String description)
	{
		try
		{
			return new Item(id, title, description, new URL("http://localhost"), LocalDateTime.now());
		}
		catch (MalformedURLException e)
		{
			//will not happen
			throw new RuntimeException(e);
		}
	}
}
