package com.richo.reader.backend;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.richo.reader.backend.exception.NoSuchUserException;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BackendTest
{
	private static final String NON_EXISTING_USER = "non_existing_user";
	private static final String ITEM_THAT_SHOULD_BE_READ = "item-id-1";
	private static final Feed FEED_1 = new Feed(
			"existing_feed_id",
			Arrays.asList(
					new Item(ITEM_THAT_SHOULD_BE_READ, "item-title-1", "item-desc-1", LocalDateTime.ofEpochSecond(100L, 0, ZoneOffset.UTC)),
					new Item("item-id-2", "item-title-2", "item-desc-2", LocalDateTime.ofEpochSecond(200L, 0, ZoneOffset.UTC)),
					new Item("item-id-3", "item-title-3", "item-desc-3", LocalDateTime.ofEpochSecond(300L, 0, ZoneOffset.UTC)),
					new Item("item-id-4", "item-title-4", "item-desc-4", LocalDateTime.ofEpochSecond(400L, 0, ZoneOffset.UTC))
			), 0L);
	private static final Feed FEED_2 = new Feed(
			"feed_2",
			Collections.singletonList(new Item("feed2-item1", "title", "desc", LocalDateTime.ofEpochSecond(100L, 0, ZoneOffset.UTC))), 0L);


	public static final User EXISTING_USER = new User("existing_user", 0L, ImmutableMap.of(FEED_1.getId(), Sets.newHashSet(ITEM_THAT_SHOULD_BE_READ), FEED_2.getId(), new HashSet<>()), new ArrayList<>());

	private Backend target;
	private UserService userService;
	private YoutubeFeedService feedService;

	@Before
	public void setUp() throws Exception
	{
		feedService = mock(YoutubeFeedService.class);
		userService = mock(UserService.class);
		target = new Backend(userService, feedService);


		when(userService.get(EXISTING_USER.getName())).thenReturn(EXISTING_USER);
		when(feedService.getChannel(FEED_1.getId())).thenReturn(Optional.of(FEED_1));
		when(feedService.getChannel(FEED_2.getId())).thenReturn(Optional.of(FEED_2));
	}

	@Test
	public void getFeedShouldReturnFeed() throws Exception
	{
		final List<com.richo.reader.model.Item> unwatchedItems = FEED_1.getItems()
				.stream()
				.filter(i -> !EXISTING_USER.isRead(FEED_1.getId(), i.getId()))
				.map(i -> new com.richo.reader.model.Item(i.getId(), i.getTitle(), i.getDescription(), i.getUploadDate().toString(), "fix url in Backend.java"))
				.collect(Collectors.toList());
		final com.richo.reader.model.Feed expected = new com.richo.reader.model.Feed(FEED_1.getId(), FEED_1.getId(), unwatchedItems);
		final Optional<com.richo.reader.model.Feed> result = target.getFeed(EXISTING_USER.getName(), FEED_1.getId());

		assertThat(result.get()).isEqualTo(expected);
	}

	@Test
	public void getFeedsShouldReturnSubscribedFeeds() throws Exception
	{
		final List<com.richo.reader.model.Feed> expected = Arrays.asList(FEED_1, FEED_2).stream()
				.map(f -> new com.richo.reader.model.Feed(f.getId(), f.getId(), f.getItems().size()))
				.collect(Collectors.toList());

		final List<com.richo.reader.model.Feed> result = target.getAllFeedsWithoutItems(EXISTING_USER.getName());

		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void getFeedsShouldNotReturnItems() throws Exception
	{
		final List<com.richo.reader.model.Feed> result = target.getAllFeedsWithoutItems(EXISTING_USER.getName());

		final List<com.richo.reader.model.Feed> expected = Arrays.asList(new com.richo.reader.model.Feed(FEED_1.getId(), FEED_1.getId(), FEED_1.getItems().size()),
				new com.richo.reader.model.Feed(FEED_2.getId(), FEED_2.getId(), FEED_2.getItems().size()));

		assertThat(result).isEqualTo(expected);
	}

	@Test(expected = NoSuchUserException.class)
	public void getFeedShouldThrowNoSuchUserExceptionIfUserDoesntExist() throws Exception
	{
		when(userService.get(NON_EXISTING_USER)).thenThrow(new NoSuchUserException(""));
		target.getFeed(NON_EXISTING_USER, FEED_1.getId());
	}

	@Test(expected = NoSuchUserException.class)
	public void getAllFeedsWithoutItemsShouldThrowNoSuchUserExceptionIfUserDoesntExist() throws Exception
	{
		when(userService.get(NON_EXISTING_USER)).thenThrow(new NoSuchUserException(""));
		target.getAllFeedsWithoutItems(NON_EXISTING_USER);
	}

	/*


	@Test(expected = NoSuchUserException.class)
	public void getFeedsShouldThrowNoSuchUserExceptionIfUserDoesntExist() throws Exception
	{
		when(userService.get(NON_EXISTING_USER)).thenThrow(new NoSuchUserException(""));
		target.getFeeds(NON_EXISTING_USER);
	}

	@Test
	public void getFeedsShouldNotReturnFeedsMarkedAsRead() throws Exception
	{
		//Standard expected feeds except we have removed ITEM_THAT_SHOULD_BE_READ
		expectedFeeds = ImmutableSet.of(new Feed(FEED_1_NAME, FEED_1_NAME, ImmutableSet.of(
				existingFeedSecondItem,
				new Item("item-id-3", "item-title-3", "item-desc-3", new URL("https://www.youtube.com/watch?v=item-id-3"), LocalDateTime.ofEpochSecond(300L, 0, ZoneOffset.UTC)),
				new Item("item-id-4", "item-title-4", "item-desc-4", new URL("https://www.youtube.com/watch?v=item-id-4"), LocalDateTime.ofEpochSecond(400L, 0, ZoneOffset.UTC))
		), ImmutableSet.of()));

		target.markAsRead(EXISTING_USER_NAME, FEED_1_NAME, ITEM_THAT_SHOULD_BE_READ);
		final Set<Feed> result = target.getFeeds(EXISTING_USER_NAME);

		assertNotNull("getFeeds returned null", result);
		assertEquals("getFeeds did not return the expected feeds", expectedFeeds, result);
	}

	@Test
	public void markAsUnreadShouldLeadToThatItemBeingReturnedAgain() throws Exception
	{
		target.markAsRead(EXISTING_USER_NAME, FEED_1_NAME, ITEM_THAT_SHOULD_BE_READ);
		target.markAsUnread(EXISTING_USER_NAME, FEED_1_NAME, ITEM_THAT_SHOULD_BE_READ);
		final Set<Feed> result = target.getFeeds(EXISTING_USER_NAME);

		assertNotNull("getFeeds returned null", result);
		assertEquals("getFeeds did not return the expected feeds", expectedFeeds, result);
	}

	@Test
	public void markOlderItemsAsUnreadShouldLeadToOlderItemsNotBeingReturned() throws Exception
	{
		//removed all items older than item 3
		expectedFeeds = ImmutableSet.of(new Feed(FEED_1_NAME, FEED_1_NAME, ImmutableSet.of(
				new Item("item-id-3", "item-title-3", "item-desc-3", new URL("https://www.youtube.com/watch?v=item-id-3"), LocalDateTime.ofEpochSecond(300L, 0, ZoneOffset.UTC)),
				new Item("item-id-4", "item-title-4", "item-desc-4", new URL("https://www.youtube.com/watch?v=item-id-4"), LocalDateTime.ofEpochSecond(400L, 0, ZoneOffset.UTC))
		), ImmutableSet.of()));

		target.markOlderItemsAsRead(EXISTING_USER_NAME, FEED_1_NAME, "item-id-3");
		final Set<Feed> result = target.getFeeds(EXISTING_USER_NAME);


		assertThat(result).isEqualTo(expectedFeeds);
	}*/
}