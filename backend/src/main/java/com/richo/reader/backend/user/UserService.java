package com.richo.reader.backend.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;

public class UserService
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final UserStorage users;

	@Inject
	public UserService(UserStorage users)
	{
		this.users = users;
	}

	public Optional<User> get(String username)
	{
		return users.get(username);
	}
}
