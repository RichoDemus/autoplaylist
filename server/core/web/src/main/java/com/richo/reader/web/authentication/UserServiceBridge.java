package com.richo.reader.web.authentication;

import com.richo.reader.backend.UserManager;
import com.richodemus.dropwizard.jwt.model.Role;
import com.richodemus.reader.dto.Password;
import com.richodemus.reader.dto.UserId;
import com.richodemus.reader.dto.Username;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Bridge between dropwizard-jwt and my backend
 */
public class UserServiceBridge implements com.richodemus.dropwizard.jwt.UserService
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final UserManager userManager;

	@Inject
	public UserServiceBridge(UserManager userManager)
	{
		this.userManager = userManager;
	}

	@Override
	public Optional<Role> login(String username, String password)
	{
		logger.info("Asked to login user {}", username);
		if (!userManager.checkCredentials(new UserId(username), password))
		{
			logger.warn("Invalid username or password for user {}", username);
			return Optional.empty();
		}
		return Optional.of(new Role("user"));
	}

	public void createUser(Username username, Password password)
	{
		userManager.createUser(username, password);
	}
}
