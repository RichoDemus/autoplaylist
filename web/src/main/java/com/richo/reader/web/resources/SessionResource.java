package com.richo.reader.web.resources;

import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.model.Session;
import com.richodemus.dropwizard.jwt.AuthenticationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/users/{username}/sessions")
@Produces(MediaType.APPLICATION_JSON)
public class SessionResource
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final AuthenticationManager authenticationManager;

	@Inject
	public SessionResource(AuthenticationManager authenticationManager)
	{
		this.authenticationManager = authenticationManager;
	}

	@POST
	@PermitAll
	public Session login(@PathParam("username") String username, String password)
	{
		logger.info("Logging user {}", username);
		try
		{
			return authenticationManager.login(username, password)
					.map(token -> new Session(username, token.getRaw()))
					.orElseThrow(() -> new NoSuchUserException("Failed to create session object"));
		}
		catch (NoSuchUserException e)
		{
			//todo probably return a response object instead,
			// or find out how to read the exception message on the client side
			logger.warn("Unable to log in user {}", username, e);
			throw new BadRequestException(e);
		}
		catch (Exception e)
		{
			logger.warn("Unable to log in user {}", username, e);
			throw new InternalServerErrorException(e);
		}
	}
}
