package com.richo.reader.web.authentication;

import com.google.common.base.Strings;
import com.richodemus.dropwizard.jwt.AuthenticationManager;
import com.richodemus.dropwizard.jwt.RawToken;
import com.richodemus.dropwizard.jwt.Token;
import com.richodemus.reader.dto.Username;
import com.richodemus.reader.user_service.UserService;
import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Makes sure that users don't try to access other users stuff <br/>
 * basically blocks if username in api/users/{username} differs from the one in the token
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
@PreMatching
public class UsernameCheckFilter implements ContainerRequestFilter
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final AuthenticationManager authenticationManager;
	private final UserService userService;

	@Inject
	public UsernameCheckFilter(final AuthenticationManager authenticationManager, final UserService userService)
	{
		this.authenticationManager = authenticationManager;
		this.userService = userService;
	}

	@Override
	public void filter(ContainerRequestContext requestContext) {
		//todo what do we do if this cast fails?
		final String path = ((ContainerRequest) requestContext).getPath(true);
		logger.debug("Checking {} for transgressions...", path);
		//todo clean this shit
		if (!path.startsWith("users"))
		{
			logger.debug("Not a user resource");
			return;
		}

		final String[] split = path.split("/");
		if (split.length < 3)
		{
			logger.debug("No username in path");
			return;
		}

		final String usernameFromURI = split[1];
		final String rawToken = requestContext.getHeaderString("x-token-jwt");
		if (Strings.isNullOrEmpty(rawToken))
		{
			logger.debug("no token");
			return;
		}

		final Token token = authenticationManager.parseToken(new RawToken(rawToken));
		final String usernameFromToken = token.getUsername();
		if (!usernameFromToken.equals(usernameFromURI))
		{
			logger.warn("username from token {} does not match username in url {}", usernameFromToken, usernameFromURI);
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
		}

		if (userService.find(new Username(usernameFromToken)) == null)
		{
			logger.warn("username from token {} does not exist", usernameFromToken);
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
		}
	}
}
