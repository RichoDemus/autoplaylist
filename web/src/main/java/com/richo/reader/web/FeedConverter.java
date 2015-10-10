package com.richo.reader.web;

import com.richo.reader.model.Feed;
import com.richo.reader.model.Item;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FeedConverter
{
	public List<Feed> convert(Set<com.richo.reader.backend.model.Feed> feeds)
	{
		return feeds.stream().map(this::toWebFeed).collect(Collectors.toList());
	}

	private Feed toWebFeed(com.richo.reader.backend.model.Feed feed)
	{
		List<Item> items = feed.getItems().stream().map(this::toWebItem).collect(Collectors.toList());
		return new Feed(feed.getName(), feed.getName(), items);
	}

	private Item toWebItem(com.richo.reader.backend.model.Item item)
	{
		return new Item(item.getVideoId(), item.getTitle(), item.getDescription(), item.getUploadDate().toString(), item.getUrl().toString());
	}
}
