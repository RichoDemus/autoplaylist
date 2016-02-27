package com.richo.reader.backend;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.richo.reader.backend.model.User;
import com.richo.reader.backend.user.UserService;
import com.richo.reader.youtube_feed_service.Feed;
import com.richo.reader.youtube_feed_service.Item;
import com.richo.reader.youtube_feed_service.YoutubeFeedService;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BackendTest
{
	private static final String NON_EXISTING_USER = "non_existing_user";
	private static final String EXISTING_FEED_ID = "existing_feed_id";
	private static final String EXISTING_FEED_FIRST_ITEM_ID = "item-id-1";
	public static final User EXISTING_USER = new User("existing_user", 0L, ImmutableMap.of(EXISTING_FEED_ID, Sets.newHashSet(EXISTING_FEED_FIRST_ITEM_ID)), new ArrayList<>());
	private static final Feed EXISTING_FEED = new Feed(
			EXISTING_FEED_ID,
			Arrays.asList(
					new Item(EXISTING_FEED_FIRST_ITEM_ID, "item-title-1", "item-desc-1", LocalDateTime.ofEpochSecond(100L, 0, ZoneOffset.UTC)),
					new Item("item-id-2", "item-title-2", "item-desc-2", LocalDateTime.ofEpochSecond(200L, 0, ZoneOffset.UTC)),
					new Item("item-id-3", "item-title-3", "item-desc-3", LocalDateTime.ofEpochSecond(300L, 0, ZoneOffset.UTC)),
					new Item("item-id-4", "item-title-4", "item-desc-4", LocalDateTime.ofEpochSecond(400L, 0, ZoneOffset.UTC))
			), 0L);

	private Backend target;
	private UserService userService;
	private YoutubeFeedService feedService;

	private List<Feed> expectedFeeds;
	private Item existingFeedSecondItem;

	@Before
	public void setUp() throws Exception
	{
		feedService = mock(YoutubeFeedService.class);
		userService = mock(UserService.class);
		target = new Backend(userService, feedService);

		existingFeedSecondItem = new Item("item-id-2", "item-title-2", "item-desc-2", LocalDateTime.ofEpochSecond(200L, 0, ZoneOffset.UTC));
		final Feed feed = new Feed(EXISTING_FEED_ID, Arrays.asList(
				new Item(EXISTING_FEED_FIRST_ITEM_ID, "item-title-1", "item-desc-1", LocalDateTime.ofEpochSecond(100L, 0, ZoneOffset.UTC)),
				existingFeedSecondItem,
				new Item("item-id-3", "item-title-3", "item-desc-3",  LocalDateTime.ofEpochSecond(300L, 0, ZoneOffset.UTC)),
				new Item("item-id-4", "item-title-4", "item-desc-4", LocalDateTime.ofEpochSecond(400L, 0, ZoneOffset.UTC))
		), 0L);
		expectedFeeds = Collections.singletonList(feed);
		when(userService.get(EXISTING_USER.getName())).thenReturn(EXISTING_USER);
		when(feedService.getChannel(EXISTING_FEED_ID)).thenReturn(Optional.of(EXISTING_FEED));
	}

	@Test
	public void getFeedShouldReturnFeed() throws Exception
	{
		final List<com.richo.reader.model.Item> unwatchedItems = EXISTING_FEED.getItems()
				.stream()
				.filter(i -> !EXISTING_USER.isRead(EXISTING_FEED.getId(), i.getId()))
				.map(i -> new com.richo.reader.model.Item(i.getId(), i.getTitle(), i.getDescription(), i.getUploadDate().toString(), "fix url in Backend.java"))
				.collect(Collectors.toList());
		final com.richo.reader.model.Feed expected = new com.richo.reader.model.Feed(EXISTING_FEED.getId(), EXISTING_FEED.getId(), unwatchedItems);
		final Optional<com.richo.reader.model.Feed> result = target.getFeed(EXISTING_USER.getName(), EXISTING_FEED.getId());

		assertThat(result.get()).isEqualTo(expected);
	}

	/*

	@Test
	public void getFeedsShouldReturnAllSubscribedFeeds() throws Exception
	{
		final Set<Feed> result = target.getFeeds(EXISTING_USER_NAME);
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

		target.markAsRead(EXISTING_USER_NAME, EXISTING_FEED_ID, EXISTING_FEED_FIRST_ITEM_ID);
		final Set<Feed> result = target.getFeeds(EXISTING_USER_NAME);

		assertNotNull("getFeeds returned null", result);
		assertEquals("getFeeds did not return the expected feeds", expectedFeeds, result);
	}

	@Test
	public void markAsUnreadShouldLeadToThatItemBeingReturnedAgain() throws Exception
	{
		target.markAsRead(EXISTING_USER_NAME, EXISTING_FEED_ID, EXISTING_FEED_FIRST_ITEM_ID);
		target.markAsUnread(EXISTING_USER_NAME, EXISTING_FEED_ID, EXISTING_FEED_FIRST_ITEM_ID);
		final Set<Feed> result = target.getFeeds(EXISTING_USER_NAME);

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

		target.markOlderItemsAsRead(EXISTING_USER_NAME, EXISTING_FEED_ID, "item-id-3");
		final Set<Feed> result = target.getFeeds(EXISTING_USER_NAME);


		assertThat(result).isEqualTo(expectedFeeds);
	}*/
}