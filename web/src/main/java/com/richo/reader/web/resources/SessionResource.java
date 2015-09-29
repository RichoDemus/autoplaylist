package com.richo.reader.web.resources;

import com.richo.reader.backend.UserManager;
import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.web.model.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Optional;

@Path("/users/{username}/sessions")
@Produces(MediaType.APPLICATION_JSON)
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
	public Session login(@PathParam("username") String username)
	{
		logger.info("Logging user {}", username);
		try
		{
			return Optional.of(username)
					.map(userManager::login)
					.map(token -> new Session(username, token))
					.orElseThrow(() -> new NoSuchUserException("Failed to create session object"));
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
