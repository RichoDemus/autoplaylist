package com.richo.reader.backend.subscription;

import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.model.User;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.ItemId;
import com.richodemus.reader.dto.UserId;
import com.richodemus.reader.dto.Username;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SubscriptionServicePort implements SubscriptionRepository
{
	private final com.richo.reader.subscription_service.SubscriptionService subscriptionService;

	@Inject
	SubscriptionServicePort(final com.richo.reader.subscription_service.SubscriptionService subscriptionService)
	{
		this.subscriptionService = subscriptionService;
	}

	@Override
	public User find(Username username) throws NoSuchUserException
	{
		final com.richo.reader.subscription_service.User user = subscriptionService.find(username);
		if (user == null)
		{
			throw new NoSuchUserException("Couldn't find user " + username);
		}

		final Map<FeedId, Set<ItemId>> feeds = convert2(user.getFeeds());
		return new User(user.getId(), new Username(user.getName().getValue()), user.getNextLabelId(), feeds, user.getLabels());
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
