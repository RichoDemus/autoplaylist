package com.richo.reader.web.resources;

import com.richo.reader.backend.Backend;
import com.richo.reader.web.model.Feed;
import com.richo.reader.web.model.FeedResult;
import com.richo.reader.web.model.Item;
import com.richo.reader.web.model.ItemOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;

@Path("/feed")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FeedResource
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Backend backend;

	@Inject
	public FeedResource(Backend injectable)
	{
		this.backend = injectable;
	}

	@GET
	public FeedResult get()
	{
		backend.getFeeds("RichoDemus");
		final Feed firstChannel = new Feed("First Channel",
				Arrays.asList(
						new Item("zbdvogFyZZM", "Dark Lord Funk - Harry Potter Parody of \"Uptown Funk\"", "Description1", "date1", "https://www.youtube.com/watch?v=zbdvogFyZZM"),
						new Item("id2", "Title2", "Description2", "date2", "https://www.youtube.com/watch?v=zbdvogFyZZM")));
		final Feed secondChannel = new Feed("Second Channel",
				Arrays.asList(
						new Item("id3", "Title3", "Description3", "date3", "https://www.youtube.com/watch?v=zbdvogFyZZM"),
						new Item("id4", "Title4", "Description4", "date4", "https://www.youtube.com/watch?v=zbdvogFyZZM")));

		return new FeedResult(Arrays.asList(firstChannel, secondChannel));
	}

	@POST
	public void performFeedOperation(ItemOperation operation)
	{
		logger.info("Received item operation {}", operation);
		switch (operation.getAction())
		{
			case "MARK_READ":
				backend.markAsRead("RichoDemus",operation.getId());
				break;
			case "MARK_UNREAD":
				backend.markAsUnread("RichoDemus",operation.getId());
				break;
			default:
				logger.error("Unknown action {}", operation.getAction());
				throw new BadRequestException("Unknown action: " + operation.getAction());
		}
	}
}
