package com.richo.reader.backend.user;

import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.model.User;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.ItemId;
import com.richodemus.reader.dto.Password;
import com.richodemus.reader.dto.UserId;
import com.richodemus.reader.dto.Username;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UserServicePort implements UserRepository
{
	private final com.richo.reader.user_service.UserService userService;

	@Inject
	UserServicePort(final com.richo.reader.user_service.UserService userService)
	{
		this.userService = userService;
	}

	@Override
	public User get(UserId username) throws NoSuchUserException
	{
		final com.richo.reader.user_service.User user = userService.get(new Username(username.getValue()));

		final Map<FeedId, Set<ItemId>> feeds = convert2(user.getFeeds());
		return new User(new UserId(user.getName().getValue()), user.getNextLabelId(), feeds, user.getLabels());
	}

	@Override
	public void update(User user)
	{
		final Username username = new Username(user.getName().getValue());
		final Map<FeedId, List<ItemId>> feeds = convert(user.getFeeds());
		userService.update(new com.richo.reader.user_service.User(username, feeds, user.getNextLabelId(), user.getLabels()));
	}

	@Override
	public boolean isPasswordValid(UserId username, String password)
	{
		return userService.isPasswordValid(new Username(username.getValue()), new Password(password));
	}

	@Override
	public void updatePassword(UserId username, String password)
	{
		userService.updatePassword(new Username(username.getValue()), new Password(password));
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
