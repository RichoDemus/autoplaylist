package com.richo.reader.web.dropwizard.autoscanned.resource;


import com.richo.reader.backend.Backend;
import com.richo.reader.backend.LabelManager;
import com.richo.reader.backend.model.Feed;
import com.richo.reader.web.TestData;
import com.richo.reader.web.dto.ItemOperation;
import com.richo.reader.web.dto.User;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.ItemId;
import com.richodemus.reader.dto.UserId;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.richo.reader.web.TestData.FEED1;
import static com.richo.reader.web.TestData.FEED2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FeedResourceTest
{
	private static final UserId USERNAME = new UserId("user");
	private static final FeedId FEED = new FeedId("my-feed");
	private static final ItemId ITEM = new ItemId("my-item");

	private static Backend backendMock = mock(Backend.class);
	private static LabelManager labelManagerMock = mock(LabelManager.class);

	@ClassRule
	public static final ResourceTestRule TARGET = ResourceTestRule.builder()
			.addResource(new FeedResource(backendMock, labelManagerMock))
			.build();

	@Before
	public void setUp() throws Exception
	{
		reset(backendMock);
		reset(labelManagerMock);
		when(backendMock.getAllFeedsWithoutItems(USERNAME)).thenReturn(TestData.FEEDS);
		when(labelManagerMock.getLabels(USERNAME)).thenReturn(Lists.emptyList());
	}

	@Test
	public void testMarkAsRead() throws Exception
	{
		TARGET.client().target("/users/" + USERNAME + "/feeds/" + FEED + "/items/" + ITEM + "/").request().post(Entity.entity(ItemOperation.MARK_AS_READ, MediaType.APPLICATION_JSON));
		verify(backendMock).markAsRead(USERNAME, FEED, ITEM);
	}

	@Test
	public void testMarkAsUnRead() throws Exception
	{
		TARGET.client().target("/users/" + USERNAME + "/feeds/" + FEED + "/items/" + ITEM + "/").request().post(Entity.entity(ItemOperation.MARK_AS_UNREAD, MediaType.APPLICATION_JSON));
		verify(backendMock).markAsUnread(USERNAME, FEED, ITEM);
	}

	@Test
	public void testMarkOlderItemsAsRead() throws Exception
	{
		TARGET.client().target("/users/" + USERNAME + "/feeds/" + FEED + "/items/" + ITEM + "/").request().post(Entity.entity(ItemOperation.MARK_OLDER_ITEMS_AS_READ, MediaType.APPLICATION_JSON));
		verify(backendMock).markOlderItemsAsRead(USERNAME, FEED, ITEM);
	}

	@Test
	public void getFeedsShouldReturnTheFeedsFromBackend() throws Exception
	{
		final User result = TARGET.client().target("/users/" + USERNAME + "/feeds/").request().get(User.class);

		//todo write better asserts
		assertEquals(TestData.FEEDS.size(), result.getFeeds().size());
		final long numberOfFeedsWithFirstId = result.getFeeds().stream().filter(f -> f.getName().equals(FEED1.getName())).count();
		final long numberOfFeedsWithSecondId = result.getFeeds().stream().filter(f -> f.getName().equals(FEED2.getName())).count();
		assertEquals(1, numberOfFeedsWithFirstId);
		assertEquals(1, numberOfFeedsWithSecondId);
	}

	@Test
	public void feedsShouldNotContainItems() throws Exception
	{
		final User result = TARGET.client().target("/users/" + USERNAME + "/feeds/").request().get(User.class);
		result.getFeeds().forEach(f -> assertEquals(0, f.getItems().size()));
	}

	@Test
	public void feedsReturnedShouldContainNumberOfAvailableItems() throws Exception
	{
		final User result = TARGET.client().target("/users/" + USERNAME + "/feeds/").request().get(User.class);

		final Feed feed1 = result.getFeeds().stream().filter(f -> f.getName().equals(FEED1.getName())).findFirst().orElseThrow(() -> new RuntimeException("Couldn't find feed1"));
		final Feed feed2 = result.getFeeds().stream().filter(f -> f.getName().equals(FEED2.getName())).findFirst().orElseThrow(() -> new RuntimeException("Couldn't find feed2"));

		assertEquals(TestData.FEED1.getNumberOfAvailableItems(), feed1.getNumberOfAvailableItems());
		assertEquals(TestData.FEED2.getNumberOfAvailableItems(), feed2.getNumberOfAvailableItems());
	}

	@Test
	public void shouldAddFeed() throws Exception
	{
		//language=JSON
		final String channelName = "\"ERB\"";
		TARGET.client().target("/users/" + USERNAME + "/feeds/").request().post(Entity.json(channelName));

		verify(backendMock).addFeed(USERNAME, new FeedId("ERB"));
	}

	@Test
	public void shouldRespondWithUserErrorWhenAddingEmptyFeed() throws Exception
	{
		final Response response = TARGET.client().target("/users/" + USERNAME + "/feeds/").request().post(Entity.json("\"\""));
		final int status = response.getStatus();
		final String body = response.readEntity(String.class);

		assertThat(status).isEqualTo(400);
		// todo see if we can get the actual error message: "FeedId can't be empty"
		//language=JSON
		final String expectedError = "{\"code\":400,\"message\":\"Unable to process JSON\"}";
		assertThat(body).isEqualTo(expectedError);
	}
}
