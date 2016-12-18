package com.richo.reader.backend.user;

import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.model.User;
import com.richodemus.reader.dto.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class InMemoryUserPersistence implements UserPersister
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Map<UserId, User> users;

	InMemoryUserPersistence()
	{
		users = new HashMap<>();
	}

	@Override
	public User get(UserId username) throws NoSuchUserException
	{
		if (!users.containsKey(username))
		{
			throw new NoSuchUserException("No such user: " + username);
		}
		return users.get(username);
	}

	@Override
	public void update(User user)
	{
		users.put(user.getName(), user);
	}

	@Override
	public boolean isPasswordValid(UserId username, String password)
	{
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public void updatePassword(UserId username, String password)
	{
		throw new IllegalStateException("Not implemented");
	}
}
