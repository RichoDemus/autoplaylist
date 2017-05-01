package com.richo.reader.backend.feed;

import com.richo.reader.backend.model.Feed;
import com.richo.reader.youtube_feed_service.Item;
import com.richo.reader.youtube_feed_service.YoutubeFeedService;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedUrl;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class FeedServicePort implements FeedRepository
{
	private final YoutubeFeedService feedService;

	@Inject
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
	public void registerChannel(FeedId feedId)
	{
		feedService.registerChannel(feedId);
	}

	@Override
	public FeedId getFeedId(FeedUrl feedUrl)
	{
		return feedService.getFeedId(feedUrl);
	}

	private List<com.richo.reader.backend.model.Item> convertItems(List<Item> items)
	{
		return items.stream()
				.map(this::convertItem)
				.collect(toList());
	}

	private com.richo.reader.backend.model.Item convertItem(Item item)
	{
		return new com.richo.reader.backend.model.Item(item.getId(), item.getTitle(), item.getDescription(), item.getUploadDate().toString(), "https://youtube.com/watch?v=" + item.getId(), item.getDuration(), item.getViews());
	}
}
