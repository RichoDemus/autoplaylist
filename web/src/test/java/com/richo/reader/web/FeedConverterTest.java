package com.richo.reader.web;

import com.google.common.collect.Sets;
import com.richo.reader.backend.model.Feed;
import com.richo.reader.backend.model.Item;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FeedConverterTest
{
	@Test
	public void testConvert() throws Exception
	{
		final Item item = new Item("id", "title", "description", new URL("http://localhost"), LocalDateTime.now());
		final Item item2 = new Item("id2", "title2", "description2", new URL("http://localhost2"), LocalDateTime.now());
		final Set<Item> items = Arrays.asList(item, item2).stream().collect(Collectors.toSet());
		final Feed feed1 = new Feed("id", "name", items, Sets.newHashSet());

		final Item item3 = new Item("id3", "title3", "description3", new URL("http://localhost3"), LocalDateTime.now());
		final Item item4 = new Item("id4", "title4", "description4", new URL("http://localhost4"), LocalDateTime.now());
		final Set<Item> items2 = Arrays.asList(item3, item4).stream().collect(Collectors.toSet());
		final Feed feed2 = new Feed("id2", "name2", items2, Sets.newHashSet());

		final Set<Feed> feedsToConvert = Arrays.asList(feed1, feed2).stream().collect(Collectors.toSet());

		final List<com.richo.reader.web.model.Feed> result = new FeedConverter().convert(feedsToConvert);

		Assert.assertNotNull(result);
		Assert.assertEquals(2, result.size());
		//TODO better asserts
	}
}