package com.richo.reader.web.resources;

import com.google.common.base.Strings;
import com.richo.reader.backend.LabelManager;
import com.richo.reader.backend.exception.NoSuchLabelException;
import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.web.LabelConverter;
import com.richo.reader.model.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/users/{username}/labels/")
@Produces(MediaType.APPLICATION_JSON)
public class LabelResource
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final LabelManager labelManager;
	private LabelConverter labelConverter;

	@Inject
	public LabelResource(LabelManager labelManager, LabelConverter labelConverter)
	{
		this.labelManager = labelManager;
		this.labelConverter = labelConverter;
	}

	@POST
	public Label createLabel(@PathParam("username") String username, String labelName)
	{
		if (Strings.isNullOrEmpty(labelName))
		{
			throw new BadRequestException("Label name can't be empty");
		}
		logger.debug("creating label {} for {}", labelName, username);
		try
		{
			return labelConverter.toWebLabel(labelManager.createLabelForUser(username, labelName));
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

	@PUT
	@Path("/{labelId}")
	public void addFeedToLabel(@PathParam("username") String username, @PathParam("labelId") long labelId, String feedId)
	{
		logger.debug("Adding feed {} to label {} for user {}", feedId, labelId, username);
		try
		{
			labelManager.addFeedToLabel(username, labelId, feedId);
		}
		catch (NoSuchUserException|NoSuchLabelException e)
		{
			throw new BadRequestException(e);
		}
	}
}
