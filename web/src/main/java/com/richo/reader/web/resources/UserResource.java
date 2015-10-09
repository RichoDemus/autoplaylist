package com.richo.reader.web.resources;

import com.richo.reader.backend.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/users")
public class UserResource
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final UserManager userManager;

	@Inject
	public UserResource(UserManager userManager)
	{
		this.userManager = userManager;
	}

	@POST
	public String createUser(String username)
	{
		try
		{
			userManager.createUser(username);
		}
		catch (Exception e)
		{
			logger.error("Exception when creating user {}", e);
			throw new InternalServerErrorException(e);
		}
		return username + " created";
	}
}
