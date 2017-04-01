package com.richo.reader.backend;

import com.google.common.collect.Sets;
import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.model.User;
import com.richo.reader.backend.user.UserRepository;
import com.richodemus.reader.dto.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;

public class UserManager
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final UserRepository userRepository;

	@Inject
	public UserManager(UserRepository userRepository)
	{
		this.userRepository = userRepository;
	}

	public void createUser(UserId username, String password)
	{
		logger.debug("Creating user {}", username);
		userRepository.update(new User(username, Sets.newHashSet()));
		userRepository.updatePassword(username, password);
	}

	public boolean checkCredentials(UserId username, String password) throws NoSuchUserException
	{
		logger.info("Checking credentials for {}", username);
		final Optional<User> maybeUser = Optional.ofNullable(userRepository.get(username));

		//todo real password management
		return maybeUser.isPresent() && userRepository.isPasswordValid(username, password);
	}
}
