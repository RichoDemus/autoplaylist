package com.richo.reader.web.dropwizard.autoscanned.resource;

import com.codahale.metrics.annotation.Timed;
import com.richo.reader.web.authentication.UserServiceBridge;
import com.richodemus.reader.web.dto.CreateUserCommand;
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
	public String createUser(final CreateUserCommand createUserCommand)
	{
//		if (!INVITE_CODE.equals(createUserCommand.getInviteCode()))
		if(true)
		{
			logger.info("{} tried to signup with invalid code {}", createUserCommand.getUsername(), createUserCommand.getInviteCode());
			throw new ForbiddenException("Signup not allowed");
		}
		try
		{
			// todo use UserId through the whole chain instead
			userService.createUser(createUserCommand.getUsername(), createUserCommand.getPassword());
		}
		catch (Exception e)
		{
			logger.error("Exception when creating user {}", e);
			throw new InternalServerErrorException(e);
		}
		return createUserCommand.getUsername() + " created";
	}
}
