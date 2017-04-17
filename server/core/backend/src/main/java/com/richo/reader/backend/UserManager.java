package com.richo.reader.backend;

import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.model.User;
import com.richo.reader.backend.user.SubscriptionRepository;
import com.richodemus.reader.dto.Password;
import com.richodemus.reader.dto.UserId;
import com.richodemus.reader.dto.Username;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;

public class UserManager
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final SubscriptionRepository subscriptionRepository;

	@Inject
	public UserManager(SubscriptionRepository subscriptionRepository)
	{
		this.subscriptionRepository = subscriptionRepository;
	}

	public void createUser(Username username, Password password)
	{
		logger.debug("Creating user {}", username);
		subscriptionRepository.create(username, password);
	}

	public boolean checkCredentials(UserId username, String password) throws NoSuchUserException
	{
		logger.info("Checking credentials for {}", username);
		final Optional<User> maybeUser = Optional.ofNullable(subscriptionRepository.get(username));

		//todo real password management
		return maybeUser.isPresent() && subscriptionRepository.isPasswordValid(username, password);
	}
}
