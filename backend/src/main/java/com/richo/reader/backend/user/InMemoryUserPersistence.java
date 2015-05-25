package com.richo.reader.backend.user;

import com.google.api.client.util.Maps;
import com.richo.reader.backend.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;

@Singleton
public class InMemoryUserPersistence implements UserPersister
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Map<String, User> users;

	InMemoryUserPersistence()
	{
		users = Maps.newHashMap();
	}

	@Override
	public Optional<User> get(String username)
	{
		return Optional.ofNullable(users.get(username));
	}

	@Override
	public void update(User user)
	{
		users.put(user.getName(), user);
	}
}
