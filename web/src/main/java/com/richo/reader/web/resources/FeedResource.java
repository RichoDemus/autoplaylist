package com.richo.reader.web.resources;

import com.richo.casinobots.api.Bot;
import com.richo.reader.web.model.Feed;
import com.richo.reader.web.model.FeedResult;
import com.richo.reader.web.model.Item;
import com.richo.reader.web.model.ItemOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Set;

@Path("/feed")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FeedResource
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final InjectMe injectable;
	private final Set<Bot> allBots;

	@Inject
	public FeedResource(InjectMe injectable, Set<Bot> allBots)
	{
		this.injectable = injectable;
		this.allBots = allBots;
	}

	@GET
	public FeedResult get()
	{
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
	}
}
