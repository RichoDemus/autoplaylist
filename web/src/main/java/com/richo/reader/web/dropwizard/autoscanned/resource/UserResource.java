package com.richo.reader.web.dropwizard.autoscanned.resource;

import com.codahale.metrics.annotation.Timed;
import com.richo.reader.web.authentication.UserServiceBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/users")
public class UserResource
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final UserServiceBridge userService;

	@Inject
	UserResource(UserServiceBridge userService)
	{
		this.userService = userService;
	}

	@Timed
	@POST
	public String createUser(String username)
	{
		try
		{
			//Hack, this will only work during testing
			logger.info("youtube_url is {}", System.getenv("YOUTUBE_URL"));
			if (System.getenv("YOUTUBE_URL") == null)
			{
				throw new BadRequestException("Not implemented");
			}
			userService.createUser(username, "123456789qwertyuio123qweasd");
		}
		catch (Exception e)
		{
			logger.error("Exception when creating user {}", e);
			throw new InternalServerErrorException(e);
		}
		return username + " created";
	}
}
