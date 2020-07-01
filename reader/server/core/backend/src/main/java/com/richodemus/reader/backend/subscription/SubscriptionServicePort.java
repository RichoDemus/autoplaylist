package com.richodemus.reader.backend.subscription;

import com.richodemus.reader.backend.exception.NoSuchUserException;
import com.richodemus.reader.backend.model.Feed;
import com.richodemus.reader.backend.model.Item;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.ItemId;
import com.richodemus.reader.dto.UserId;
import com.richodemus.reader.subscription_service.SubscriptionService;
import com.richodemus.reader.subscription_service.User;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Repository
public class SubscriptionServicePort implements SubscriptionRepository
{
	private final SubscriptionService subscriptionService;

	SubscriptionServicePort(final SubscriptionService subscriptionService)
	{
		this.subscriptionService = subscriptionService;
	}


	@Override
	public List<Feed> get(UserId userId)
	{
		final User user = subscriptionService.get(userId);
		if (user == null)
		{
			throw new NoSuchUserException("No such user " + userId);
		}

		return user.getFeeds().stream()
				.map(feed ->
				{
					final FeedId id = feed.getId();
					final List<Item> watchedItems = feed.getWatchedItems().stream().map(i -> new Item(i, "", "", "", "", Duration.ZERO, 0L)).collect(toList());
					return new Feed(id, null, watchedItems);
				})
				.collect(toList());
	}

	@Override
	public List<ItemId> get(UserId userId, FeedId feedId)
	{
		final User user = subscriptionService.get(userId);
		if (user == null)
		{
			throw new NoSuchUserException("No such user " + userId);
		}

		return user.getFeeds().stream()
				.filter(feed -> feed.getId().equals(feedId))
				.map(com.richodemus.reader.subscription_service.Feed::getWatchedItems)
				.findAny()
				.orElseThrow(() -> new IllegalStateException("User " + userId + " not subscribed to " + feedId));
	}

	@Override
	public void subscribe(UserId userId, FeedId feedId)
	{
		subscriptionService.subscribe(userId, feedId);
	}

	@Override
	public void markAsRead(UserId userId, FeedId feedId, ItemId itemId)
	{
		subscriptionService.markAsRead(userId, feedId, itemId);
	}

	@Override
	public void markAsUnread(UserId userId, FeedId feedId, ItemId itemId)
	{
		subscriptionService.markAsUnread(userId, feedId, itemId);
	}

	private Map<FeedId, Set<ItemId>> convert2(Map<FeedId, List<ItemId>> feeds)
	{
		return feeds.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new HashSet<>(e.getValue())));
	}
}
