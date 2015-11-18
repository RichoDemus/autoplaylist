package com.richo.reader.web.resources;

import com.richo.reader.web.authentication.UserServiceBridge;
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
	private final UserServiceBridge userService;

	@Inject
	public UserResource(UserServiceBridge userService)
	{
		this.userService = userService;
	}

	@POST
	public String createUser(String username)
	{
		try
		{
			userService.createUser(username);
		}
		catch (Exception e)
		{
			logger.error("Exception when creating user {}", e);
			throw new InternalServerErrorException(e);
		}
		return username + " created";
	}
}
