package com.richodemus.reader.backend.feed;

import com.richodemus.reader.backend.model.Feed;
import com.richodemus.reader.backend.model.Item;
import com.richodemus.reader.youtube_feed_service.YoutubeFeedService;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedUrl;
import com.richodemus.reader.dto.ItemId;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Repository
public class FeedServicePort implements FeedRepository
{
	private final YoutubeFeedService feedService;

	public FeedServicePort(YoutubeFeedService feedService)
	{
		this.feedService = feedService;
	}

	@Override
	public Optional<Feed> getFeed(FeedId feedId)
	{
		return feedService.getChannel(feedId)
				.map(feed -> new Feed(feed.getId(), feed.getName(), convertItems(feed.getItems())));
	}

	@Override
	public List<ItemId> getItemIds(FeedId feedId)
	{
		return feedService.getChannel(feedId)
				.map(com.richodemus.reader.youtube_feed_service.Feed::getItems)
				.orElseGet(ArrayList::new)
				.stream()
				.map(com.richodemus.reader.youtube_feed_service.Item::getId)
				.collect(toList());
	}

	@Override
	public FeedId getFeedId(FeedUrl feedUrl)
	{
		return feedService.getFeedId(feedUrl);
	}

	private List<Item> convertItems(List<com.richodemus.reader.youtube_feed_service.Item> items)
	{
		return items.stream()
				.map(this::convertItem)
				.collect(toList());
	}

	private Item convertItem(com.richodemus.reader.youtube_feed_service.Item item)
	{
		return new Item(item.getId(), item.getTitle(), item.getDescription(), item.getUploadDate().toString(), "https://youtube.com/watch?v=" + item.getId(), item.getDuration(), item.getViews());
	}
}
