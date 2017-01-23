package com.richo.reader.backend;

import com.google.common.collect.Sets;
import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.model.User;
import com.richo.reader.backend.user.UserServicePort;
import com.richodemus.reader.dto.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;

public class UserManager
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final UserServicePort userServicePort;

	@Inject
	public UserManager(UserServicePort userServicePort)
	{
		this.userServicePort = userServicePort;
	}

	public void createUser(UserId username, String password)
	{
		logger.debug("Creating user {}", username);
		userServicePort.update(new User(username, Sets.newHashSet()));
		userServicePort.updatePassword(username, password);
	}

	public boolean checkCredentials(UserId username, String password) throws NoSuchUserException
	{
		logger.info("Checking credentials for {}", username);
		final Optional<User> maybeUser = Optional.ofNullable(userServicePort.get(username));

		//todo real password management
		return maybeUser.isPresent() && userServicePort.isPasswordValid(username, password);
	}
}
