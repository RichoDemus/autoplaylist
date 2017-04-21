package com.richo.reader.backend.subscription;

import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.model.User;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.ItemId;
import com.richodemus.reader.dto.UserId;
import com.richodemus.reader.dto.Username;

import javax.inject.Inject;
import java.util.ArrayList;
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
	public User get(UserId username) throws NoSuchUserException
	{
		final com.richo.reader.subscription_service.User user = subscriptionService.find(new Username(username.getValue()));
		if (user == null)
		{
			throw new NoSuchUserException("Couldn't find user " + username);
		}

		final Map<FeedId, Set<ItemId>> feeds = convert2(user.getFeeds());
		return new User(user.getId(), new UserId(user.getName().getValue()), user.getNextLabelId(), feeds, user.getLabels());
	}

	@Override
	public void update(User user)
	{
		final Username username = new Username(user.getName().getValue());
		final Map<FeedId, List<ItemId>> feeds = convert(user.getFeeds());
		subscriptionService.update(new com.richo.reader.subscription_service.User(user.id, username, feeds, user.getNextLabelId(), user.getLabels()));
	}

	private Map<FeedId, List<ItemId>> convert(Map<FeedId, Set<ItemId>> feeds)
	{
		return feeds.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue())));
	}

	private Map<FeedId, Set<ItemId>> convert2(Map<FeedId, List<ItemId>> feeds)
	{
		return feeds.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new HashSet<>(e.getValue())));
	}
}
