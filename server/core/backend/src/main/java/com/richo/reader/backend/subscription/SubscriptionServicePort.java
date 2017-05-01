package com.richo.reader.backend.subscription;

import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.model.Feed;
import com.richo.reader.backend.model.Item;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.ItemId;
import com.richodemus.reader.dto.UserId;

import javax.inject.Inject;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class SubscriptionServicePort implements SubscriptionRepository
{
	private final com.richo.reader.subscription_service.SubscriptionService subscriptionService;

	@Inject
	SubscriptionServicePort(final com.richo.reader.subscription_service.SubscriptionService subscriptionService)
	{
		this.subscriptionService = subscriptionService;
	}


	@Override
	public List<Feed> get(UserId userId)
	{
		final com.richo.reader.subscription_service.User user = subscriptionService.get(userId);
		if (user == null)
		{
			throw new NoSuchUserException("No such user " + userId);
		}

		return user.getFeeds().entrySet().stream()
				.map(entry ->
				{
					final FeedId id = entry.getKey();
					final List<Item> watchedItems = entry.getValue().stream().map(i -> new Item(i, "", "", "", "", Duration.ZERO, 0L)).collect(toList());
					return new Feed(id, null, watchedItems);
				})
				.collect(toList());
	}

	@Override
	public List<ItemId> get(UserId userId, FeedId feedId)
	{
		final com.richo.reader.subscription_service.User user = subscriptionService.get(userId);
		if (user == null)
		{
			throw new NoSuchUserException("No such user " + userId);
		}

		return user.getFeeds().entrySet().stream()
				.filter(feed -> feed.getKey().equals(feedId))
				.map(Map.Entry::getValue)
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
