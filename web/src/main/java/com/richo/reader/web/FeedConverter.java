package com.richo.reader.web;

import com.richo.reader.web.model.Feed;
import com.richo.reader.web.model.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FeedConverter
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

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
