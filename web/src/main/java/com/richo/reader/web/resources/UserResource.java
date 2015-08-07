package com.richo.reader.web.resources;

import com.richo.reader.backend.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
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
		userManager.createUser(username);
		return username + " created";
	}
}
