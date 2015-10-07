package com.richo.reader.web.resources;


import com.richo.reader.backend.Backend;
import com.richo.reader.web.model.ItemOperation;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

public class FeedResourceTest
{
	public static final String USERNAME = "user";
	public static final String FEED = "my-feed";
	public static final String ITEM = "my-item";
	private static Backend backendMock = mock(Backend.class);

	@ClassRule
	public static final ResourceTestRule RESOURCES = ResourceTestRule.builder()
			.addResource(new FeedResource(backendMock, null, null, null))
			.build();

	@Before
	public void setUp() throws Exception
	{
		reset(backendMock);
	}

	@Test
	public void testMarkAsRead() throws Exception
	{
		RESOURCES.client().target("/users/" + USERNAME + "/feeds/" + FEED + "/items/" + ITEM + "/").request().post(Entity.entity(new ItemOperation("MARK_READ"), MediaType.APPLICATION_JSON));
		verify(backendMock).markAsRead(USERNAME, FEED, ITEM);
	}

	@Test
	public void testMarkAsUnRead() throws Exception
	{
		RESOURCES.client().target("/users/" + USERNAME + "/feeds/" + FEED + "/items/" + ITEM + "/").request().post(Entity.entity(new ItemOperation("MARK_UNREAD"), MediaType.APPLICATION_JSON));
		verify(backendMock).markAsUnread(USERNAME, FEED, ITEM);
	}
}
