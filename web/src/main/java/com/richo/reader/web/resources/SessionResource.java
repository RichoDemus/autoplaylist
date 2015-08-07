package com.richo.reader.web.resources;

import com.richo.reader.backend.UserManager;
import com.richo.reader.backend.exception.NoSuchUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/sessions")
public class SessionResource
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final UserManager userManager;

	@Inject
	public SessionResource(UserManager userManager)
	{
		this.userManager = userManager;
	}

	@POST
	public String login(String username)
	{
		logger.info("Logging user {}", username);
		try
		{
			return userManager.login(username);
		}
		catch (NoSuchUserException e)
		{
			//todo probably return a response object instead,
			// or find out how to read the exception message on the client side
			logger.warn("Unable to log in user {}", username, e);
			throw new BadRequestException(e.getMessage());
		}
	}
}
