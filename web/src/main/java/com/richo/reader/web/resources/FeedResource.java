package com.richo.reader.web.resources;

import com.richo.reader.backend.Backend;
import com.richo.reader.backend.LabelManager;
import com.richo.reader.backend.exception.ItemNotInFeedException;
import com.richo.reader.backend.exception.NoSuchChannelException;
import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.exception.UserNotSubscribedToThatChannelException;
import com.richo.reader.backend.model.Label;
import com.richo.reader.web.FeedConverter;
import com.richo.reader.web.LabelConverter;
import com.richo.reader.model.ItemOperation;
import com.richo.reader.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Set;

@Path("/users/{username}/feeds/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FeedResource
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Backend backend;
	private final LabelManager labelManager;
	private final FeedConverter feedConverter;
	private final LabelConverter labelConverter;

	@Inject
	public FeedResource(Backend injectable, LabelManager labelManager, FeedConverter feedConverter, LabelConverter labelConverter)
	{
		this.backend = injectable;
		this.labelManager = labelManager;
		this.feedConverter = feedConverter;
		this.labelConverter = labelConverter;
	}

	@GET
	public User get(@PathParam("username") final String username)
	{
		final Set<com.richo.reader.backend.model.Feed> feeds;
		final List<Label> labels;
		try
		{
			feeds = backend.getFeeds(username);
			labels = labelManager.getLabels(username);
		}
		catch (NoSuchUserException e)
		{
			logger.warn("Exception when {} got all feeds", e);
			throw new BadRequestException(e);
		}
		catch (Exception e)
		{
			logger.warn("Exception when {} got all feeds", e);
			throw new InternalServerErrorException(e);
		}
		return new User(feedConverter.convert(feeds), labelConverter.convert(labels));
	}

	@POST
	@Path("/{feed}/items/{item}/")
	public void performFeedOperation(@PathParam("username") final String username, @PathParam("feed") final String feedId, @PathParam("item") final String itemId, ItemOperation operation)
	{
		logger.info("Received item operation {} for feed {}, item {}", operation, feedId, itemId);
		try
		{
			switch (operation.getAction())
			{
				case MARK_READ:
					backend.markAsRead(username, feedId, itemId);
					break;
				case MARK_UNREAD:
					backend.markAsUnread(username, feedId, itemId);
					break;
				case MARK_OLDER_ITEMS_AS_READ:
					backend.markOlderItemsAsRead(username, feedId, itemId);
					break;
				default:
					logger.error("Unknown action {}", operation.getAction());
					throw new BadRequestException("Unknown action: " + operation.getAction());
			}
		}
		catch (NoSuchUserException | UserNotSubscribedToThatChannelException | NoSuchChannelException | ItemNotInFeedException e)
		{
			logger.warn("Exception when {} performed operation {} on feed {}", username, operation, feedId, e);
			throw new BadRequestException(e);
		}
		catch (Exception e)
		{
			logger.warn("Exception when {} performed operation {} on feed {}", username, operation, feedId, e);
			throw new InternalServerErrorException(e);
		}
	}

	@POST //todo shouldnt this be a put
	public void addFeed(@PathParam("username") final String username, final String feedName)
	{
		try
		{
			backend.addFeed(username, feedName);
		}
		catch (NoSuchChannelException | NoSuchUserException e)
		{
			logger.warn("Exception when {} added feed {}", username, feedName, e);
			throw new BadRequestException(e);
		}
		catch (Exception e)
		{
			logger.warn("Exception when {} added feed {}", username, feedName, e);
			throw new InternalServerErrorException(e);
		}
	}
}
