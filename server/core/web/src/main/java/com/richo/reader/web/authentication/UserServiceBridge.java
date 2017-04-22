package com.richo.reader.web.authentication;

import com.richodemus.dropwizard.jwt.model.Role;
import com.richodemus.reader.dto.Password;
import com.richodemus.reader.dto.Username;
import com.richodemus.reader.user_service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Bridge between dropwizard-jwt and the user service
 */
public class UserServiceBridge implements com.richodemus.dropwizard.jwt.UserService
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final UserService userService;

	@Inject
	public UserServiceBridge(final UserService userService)
	{
		this.userService = userService;
	}

	@Override
	public Optional<Role> login(String username, String password)
	{
		logger.info("Asked to login user {}", username);
		if (!userService.passwordValid(new Username(username), new Password(password)))
		{
			logger.warn("Invalid username or password for user {}", username);
			return Optional.empty();
		}
		return Optional.of(new Role("user"));
	}

	public void createUser(Username username, Password password)
	{
		userService.create(username, password);
	}
}
