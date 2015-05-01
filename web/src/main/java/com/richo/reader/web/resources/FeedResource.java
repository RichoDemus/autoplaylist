package com.richo.reader.web.resources;

import com.richo.reader.backend.Backend;
import com.richo.reader.backend.exception.NoSuchChannelException;
import com.richo.reader.web.FeedConverter;
import com.richo.reader.web.model.FeedResult;
import com.richo.reader.web.model.ItemOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Set;

@Path("/feeds")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FeedResource
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Backend backend;
	private final FeedConverter feedConverter;

	@Inject
	public FeedResource(Backend injectable, FeedConverter feedConverter)
	{
		this.backend = injectable;
		this.feedConverter = feedConverter;
	}

	@GET
	@Path("/users/{username}/feeds/")
	public FeedResult get(@PathParam("username") final String username)
	{
		final Set<com.richo.reader.backend.model.Feed> feeds = backend.getFeeds(username);
		return new FeedResult(feedConverter.convert(feeds));
	}

	@POST
	@Path("/users/{username}/feeds/{feed}/items/{item}/")
	public void performFeedOperation(@PathParam("username") final String username, @PathParam("feed") final String feedId, @PathParam("item") final String itemId, ItemOperation operation)
	{
		logger.info("Received item operation {} for feed {}, item {}", operation, feedId, itemId);
		switch (operation.getAction())
		{
			case "MARK_READ":
				backend.markAsRead(username, feedId, itemId);
				break;
			case "MARK_UNREAD":
				backend.markAsUnread(username, feedId, itemId);
				break;
			default:
				logger.error("Unknown action {}", operation.getAction());
				throw new BadRequestException("Unknown action: " + operation.getAction());
		}
	}

	@POST
	@Path("/users/{username}/feeds/")
	public void addFeed(@PathParam("username")final String username, final String feedName)
	{
		try
		{
			backend.addFeed(username, feedName);
		}
		catch (NoSuchChannelException e)
		{
			throw new BadRequestException(e.getMessage());
		}
	}
}
