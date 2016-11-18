package com.richo.reader.web.authentication;

import com.google.common.base.Strings;
import com.richodemus.dropwizard.jwt.AuthenticationManager;
import com.richodemus.dropwizard.jwt.RawToken;
import com.richodemus.dropwizard.jwt.Token;
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

	@Inject
	public UsernameCheckFilter(final AuthenticationManager authenticationManager)
	{
		this.authenticationManager = authenticationManager;
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException
	{
		logger.debug("Checking for transgressions...");
		//todo what do we do if this cast fails?
		//todo clean this shit
		final String uri = ((ContainerRequest) requestContext).getAbsolutePath().toString();
		final String[] split = uri.split("/");
		if (split.length < 6)
		{
			logger.debug("No username in path");
			return;
		}
		final String usernameFromURI = split[5];
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
	}
}
