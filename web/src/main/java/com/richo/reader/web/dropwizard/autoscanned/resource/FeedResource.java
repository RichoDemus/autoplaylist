package com.richo.reader.web.dropwizard.autoscanned.resource;

import com.codahale.metrics.annotation.Timed;
import com.richo.reader.backend.Backend;
import com.richo.reader.backend.LabelManager;
import com.richo.reader.backend.exception.ItemNotInFeedException;
import com.richo.reader.backend.exception.NoSuchChannelException;
import com.richo.reader.backend.exception.NoSuchLabelException;
import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.exception.UserNotSubscribedToThatChannelException;
import com.richo.reader.backend.model.Feed;
import com.richo.reader.backend.model.FeedWithoutItems;
import com.richo.reader.backend.model.Label;
import com.richo.reader.web.dto.ItemOperation;
import com.richo.reader.web.dto.User;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.ItemId;
import com.richodemus.reader.dto.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
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
import java.util.ArrayList;
import java.util.List;

@Path("/users/{username}/feeds/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class FeedResource
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Backend backend;
	private final LabelManager labelManager;

	@Inject
	FeedResource(final Backend injectable, final LabelManager labelManager)
	{
		this.backend = injectable;
		this.labelManager = labelManager;
	}

	@Timed
	@GET
	public User getAllFeedsAndLabels(@PathParam("username") final UserId username)
	{
		try
		{
			final List<FeedWithoutItems> feeds = backend.getAllFeedsWithoutItems(username);
			final List<Label> labels = labelManager.getLabels(username);
			List<Holder> jobs = new ArrayList<>();
			labels.forEach(label ->
			{
				label.getFeeds().stream().map(feedId ->
				{
					try
					{
						return backend.feedNameToId(feedId);
					}
					catch (Exception e)
					{
						logger.error("Unable to convert feedname {}", feedId);
						return feedId;
					}
				}).forEach(l ->
				{

					jobs.add(new Holder((username), label.getId(), l));

				});
			});

			jobs.forEach(job ->
			{
				try
				{
					labelManager.addFeedToLabel(job.username, job.id, job.l);
				}
				catch (NoSuchLabelException e)
				{
					logger.error("Failed to convert label {}", job.id);
				}
			});
			return new User(feeds, labels);
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
	}

	private static class Holder
	{

		private final UserId username;
		private final long id;
		private final FeedId l;

		public Holder(UserId username, long id, FeedId l)
		{

			this.username = username;
			this.id = id;
			this.l = l;
		}
	}

	@Timed
	@GET
	@Path("/{feed}/")
	public Feed getFeed(@PathParam("username") final UserId username, @PathParam("feed") final FeedId feedId)
	{
		return backend.getFeed(username, feedId)
				.orElseThrow(() -> new BadRequestException("Couldn't find feed " + feedId));
	}

	@Timed
	@POST
	@Path("/{feed}/items/{item}/")
	public void performFeedOperation(@PathParam("username") final UserId username, @PathParam("feed") final FeedId feedId, @PathParam("item") final ItemId itemId, final ItemOperation operation)
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

	@Timed
	@POST //todo shouldnt this be a put
	public void addFeed(@PathParam("username") final UserId username, final FeedId feedId)
	{
		if (feedId == null)
		{
			logger.info("User {} tried to add an empty feed", username);
			throw new BadRequestException("Feed can't be empty");
		}
		logger.info("{} wants to subscribe to {}", username, feedId);
		try
		{
			backend.addFeed(username, feedId);
		}
		catch (NoSuchChannelException | NoSuchUserException e)
		{
			logger.warn("Exception when {} added feed {}", username, feedId, e);
			throw new BadRequestException(e);
		}
		catch (Exception e)
		{
			logger.warn("Exception when {} added feed {}", username, feedId, e);
			throw new InternalServerErrorException(e);
		}
	}
}
