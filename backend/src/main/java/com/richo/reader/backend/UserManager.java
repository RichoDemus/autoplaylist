package com.richo.reader.backend;

import com.google.common.collect.Sets;
import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.model.User;
import com.richo.reader.backend.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;

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

	public boolean checkCredentials(String username, String password) throws NoSuchUserException
	{
		logger.info("Checking credentials for {}", username);
		final Optional<User> maybeUser = Optional.ofNullable(userService.get(username));

		//todo real password management
		return maybeUser.isPresent() && userService.isPasswordValid(username, password);
	}
}
