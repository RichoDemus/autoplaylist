package com.richo.reader.web.dropwizard.autoscanned.resource;

import com.codahale.metrics.annotation.Timed;
import com.richo.reader.web.authentication.UserServiceBridge;
import com.richo.reader.web.dto.CreateUserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/users")
public class UserResource
{
	private static final String INVITE_CODE = "iwouldlikeaninvitepleaseletmesignuptotestthis";
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final UserServiceBridge userService;

	@Inject
	UserResource(final UserServiceBridge userService)
	{
		this.userService = userService;
	}

	@Timed
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public String createUser(final CreateUserRequest createUserRequest)
	{
		if (!INVITE_CODE.equals(createUserRequest.inviteCode))
		{
			logger.info("{} tried to signup with invalid code {}", createUserRequest.username, createUserRequest.inviteCode);
			throw new ForbiddenException("Signup not allowed");
		}
		try
		{
			userService.createUser(createUserRequest.username, createUserRequest.password);
		}
		catch (Exception e)
		{
			logger.error("Exception when creating user {}", e);
			throw new InternalServerErrorException(e);
		}
		return createUserRequest.username + " created";
	}
}
