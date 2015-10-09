package com.richo.reader.backend;

import com.google.common.collect.ImmutableSet;
import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.model.Feed;
import com.richo.reader.backend.model.Item;
import com.richo.reader.backend.model.User;
import com.richo.reader.backend.user.UserService;
import com.richo.reader.backend.youtube.YoutubeChannelService;
import com.richo.reader.backend.youtube.model.YoutubeChannel;
import com.richo.reader.backend.youtube.model.YoutubeVideo;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BackendTest
{
	private static final String NON_EXISTING_USER = "non_existing_user";
	private static final String EXISTING_USER = "existing_user";
	private static final String EXISTING_FEED_ID = "existing_feed_id";
	private static final String EXISTING_FEED_FIRST_ITEM_ID = "item-id-1";
	private static final YoutubeChannel EXISTING_FEED = new YoutubeChannel(
			EXISTING_FEED_ID,
			ImmutableSet.of(
					new YoutubeVideo("item-title-1", "item-desc-1", EXISTING_FEED_FIRST_ITEM_ID, LocalDateTime.ofEpochSecond(100L, 0, ZoneOffset.UTC)),
					new YoutubeVideo("item-title-2", "item-desc-2", "item-id-2", LocalDateTime.ofEpochSecond(200L, 0, ZoneOffset.UTC)),
					new YoutubeVideo("item-title-3", "item-desc-3", "item-id-3", LocalDateTime.ofEpochSecond(300L, 0, ZoneOffset.UTC)),
					new YoutubeVideo("item-title-4", "item-desc-4", "item-id-4", LocalDateTime.ofEpochSecond(400L, 0, ZoneOffset.UTC))
			));

	private Backend target;
	private UserService userService;
	private YoutubeChannelService channelService;

	private Set<Feed> expectedFeeds;
	private Item existingFeedSecondItem;

	@Before
	public void setUp() throws Exception
	{
		channelService = mock(YoutubeChannelService.class);
		userService = mock(UserService.class);
		target = new Backend(userService, channelService);

		existingFeedSecondItem = new Item("item-id-2", "item-title-2", "item-desc-2", new URL("https://www.youtube.com/watch?v=item-id-2"), LocalDateTime.ofEpochSecond(200L, 0, ZoneOffset.UTC));
		final Feed feed = new Feed(EXISTING_FEED_ID, EXISTING_FEED_ID, ImmutableSet.of(
				new Item(EXISTING_FEED_FIRST_ITEM_ID, "item-title-1", "item-desc-1", new URL("https://www.youtube.com/watch?v=item-id-1"), LocalDateTime.ofEpochSecond(100L, 0, ZoneOffset.UTC)),
				existingFeedSecondItem,
				new Item("item-id-3", "item-title-3", "item-desc-3", new URL("https://www.youtube.com/watch?v=item-id-3"), LocalDateTime.ofEpochSecond(300L, 0, ZoneOffset.UTC)),
				new Item("item-id-4", "item-title-4", "item-desc-4", new URL("https://www.youtube.com/watch?v=item-id-4"), LocalDateTime.ofEpochSecond(400L, 0, ZoneOffset.UTC))
		), ImmutableSet.of());
		expectedFeeds = ImmutableSet.of(feed);
		when(userService.get(EXISTING_USER)).thenReturn(new User(EXISTING_USER, ImmutableSet.of(EXISTING_FEED_ID)));
		when(channelService.getChannelByName(EXISTING_FEED_ID)).thenReturn(Optional.of(EXISTING_FEED));
		when(channelService.getFeedById(EXISTING_FEED_ID)).thenReturn(Optional.of(feed));
	}

	@Test
	public void getFeedsShouldReturnAllSubscribedFeeds() throws Exception
	{
		final Set<Feed> result = target.getFeeds(EXISTING_USER);
		assertThat(result).isEqualTo(expectedFeeds);
	}

	@Test(expected = NoSuchUserException.class)
	public void getFeedsShouldThrowNoSuchUserExceptionIfUserDoesntExist() throws Exception
	{
		when(userService.get(NON_EXISTING_USER)).thenThrow(new NoSuchUserException(""));
		target.getFeeds(NON_EXISTING_USER);
	}

	@Test
	public void getFeedsShouldNotReturnFeedsMarkedAsRead() throws Exception
	{
		//Standard expected feeds except we have removed EXISTING_FEED_FIRST_ITEM_ID
		expectedFeeds = ImmutableSet.of(new Feed(EXISTING_FEED_ID, EXISTING_FEED_ID, ImmutableSet.of(
				existingFeedSecondItem,
				new Item("item-id-3", "item-title-3", "item-desc-3", new URL("https://www.youtube.com/watch?v=item-id-3"), LocalDateTime.ofEpochSecond(300L, 0, ZoneOffset.UTC)),
				new Item("item-id-4", "item-title-4", "item-desc-4", new URL("https://www.youtube.com/watch?v=item-id-4"), LocalDateTime.ofEpochSecond(400L, 0, ZoneOffset.UTC))
		), ImmutableSet.of()));

		target.markAsRead(EXISTING_USER, EXISTING_FEED_ID, EXISTING_FEED_FIRST_ITEM_ID);
		final Set<Feed> result = target.getFeeds(EXISTING_USER);

		assertNotNull("getFeeds returned null", result);
		assertEquals("getFeeds did not return the expected feeds", expectedFeeds, result);
	}

	@Test
	public void markAsUnreadShouldLeadToThatItemBeingReturnedAgain() throws Exception
	{
		target.markAsRead(EXISTING_USER, EXISTING_FEED_ID, EXISTING_FEED_FIRST_ITEM_ID);
		target.markAsUnread(EXISTING_USER, EXISTING_FEED_ID, EXISTING_FEED_FIRST_ITEM_ID);
		final Set<Feed> result = target.getFeeds(EXISTING_USER);

		assertNotNull("getFeeds returned null", result);
		assertEquals("getFeeds did not return the expected feeds", expectedFeeds, result);
	}

	@Test
	public void markOlderItemsAsUnreadShouldLeadToOlderItemsNotBeingReturned() throws Exception
	{
		//removed all items older than item 3
		expectedFeeds = ImmutableSet.of(new Feed(EXISTING_FEED_ID, EXISTING_FEED_ID, ImmutableSet.of(
				new Item("item-id-3", "item-title-3", "item-desc-3", new URL("https://www.youtube.com/watch?v=item-id-3"), LocalDateTime.ofEpochSecond(300L, 0, ZoneOffset.UTC)),
				new Item("item-id-4", "item-title-4", "item-desc-4", new URL("https://www.youtube.com/watch?v=item-id-4"), LocalDateTime.ofEpochSecond(400L, 0, ZoneOffset.UTC))
		), ImmutableSet.of()));

		target.markOlderItemsAsRead(EXISTING_USER, EXISTING_FEED_ID, "item-id-3");
		final Set<Feed> result = target.getFeeds(EXISTING_USER);


		assertThat(result).isEqualTo(expectedFeeds);
	}
}