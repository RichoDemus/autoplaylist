package com.richo.reader.web.dropwizard.autoscanned.resource;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
import com.richo.reader.backend.LabelManager;
import com.richo.reader.backend.exception.NoSuchUserException;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.Label;
import com.richodemus.reader.dto.LabelId;
import com.richodemus.reader.dto.Username;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/users/{username}/labels/")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class LabelResource
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final LabelManager labelManager;

	@Inject
	LabelResource(final LabelManager labelManager)
	{
		this.labelManager = labelManager;
	}

	@Timed
	@POST
	public Label createLabel(@PathParam("username") final Username username, final String labelName)
	{
		if (Strings.isNullOrEmpty(labelName))
		{
			throw new BadRequestException("Label name can't be empty");
		}
		logger.debug("creating label {} for {}", labelName, username);
		try
		{
			return labelManager.createLabelForUser(username, labelName);
		}
		catch (NoSuchUserException e)
		{
			logger.error("Exception when {} created label {}", username, labelName, e);
			throw new BadRequestException(e);
		}
		catch (Exception e)
		{
			logger.error("Exception when {} created label {}", username, labelName, e);
			throw new InternalServerErrorException(e);
		}
	}

	@Timed
	@Consumes(MediaType.APPLICATION_JSON)
	@PUT
	@Path("/{labelId}")
	public void addFeedToLabel(@PathParam("username") final Username username, @PathParam("labelId") final String labelId, final FeedId feedId)
	{
		logger.debug("Adding feed {} to label {} for user {}", feedId, labelId, username);
		try
		{
			labelManager.addFeedToLabel(new LabelId(UUID.fromString(labelId)), feedId);
		}
		catch (NoSuchUserException e)
		{
			throw new BadRequestException(e);
		}
	}
}