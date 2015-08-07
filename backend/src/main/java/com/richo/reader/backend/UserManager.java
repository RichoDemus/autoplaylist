package com.richo.reader.backend;

import com.google.common.collect.Sets;
import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.model.User;
import com.richo.reader.backend.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class UserManager
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final UserService userService;

	@Inject
	public UserManager(UserService userService)
	{
		this.userService = userService;
	}

	public void createUser(String username)
	{
		logger.debug("Creating user {}", username);
		userService.update(new User(username, Sets.newHashSet()));
	}

	public String login(String username) throws NoSuchUserException
	{
		logger.debug("Logging in {}", username);
		return userService.get(username).getName() + "-token";
	}
}
