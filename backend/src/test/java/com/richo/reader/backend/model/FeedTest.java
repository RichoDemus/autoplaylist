package com.richo.reader.backend.model;


import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Set;

public class FeedTest
{
	@Test
	public void testAddItemsToEmptyFeed() throws Exception
	{
		final Feed target = new Feed("id", "name");
		target.addNewItems(Sets.newHashSet(new Item("id", "title", "desc", new URL("http://localhost"), LocalDateTime.now()), new Item("id2", "title2", "desc2", new URL("http://localhost2"), LocalDateTime.now())));

		Assert.assertEquals(2, target.getItems().size());
	}

	@Test
	public void testAddItemsShouldNotAddAlreadyAddedItems() throws Exception
	{
		final Set<Item> items = Sets.newHashSet(new Item("id", "title", "desc", new URL("http://localhost"), LocalDateTime.now()), new Item("id2", "title2", "desc2", new URL("http://localhost2"), LocalDateTime.now()));

		final Feed target = new Feed("id", "name");
		target.addNewItems(items);
		Assert.assertEquals(2, target.getItems().size());

		target.addNewItems(items);
		Assert.assertEquals(2, target.getItems().size());
	}

	@Test
	public void testAddItemsShouldNotAddItemsThatWereAlreadyWatched() throws Exception
	{
		final Set<Item> items = Sets.newHashSet(new Item("id", "title", "desc", new URL("http://localhost"), LocalDateTime.now()), new Item("id2", "title2", "desc2", new URL("http://localhost2"), LocalDateTime.now()));
		final Feed target = new Feed("id", "name");

		target.markAsRead("id");

		target.addNewItems(items);

		Assert.assertEquals(1, target.getItems().size());
		Assert.assertEquals("id2", target.getItems().toArray(new Item[target.getItems().size()])[0].getVideoId());

	}
}