package com.richo.reader.backend;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.model.FeedWithoutItems;
import com.richo.reader.backend.model.User;
import com.richo.reader.backend.subscription.SubscriptionRepository;
import com.richo.reader.youtube_feed_service.Feed;
import com.richo.reader.youtube_feed_service.Item;
import com.richo.reader.youtube_feed_service.YoutubeFeedService;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedName;
import com.richodemus.reader.dto.FeedUrl;
import com.richodemus.reader.dto.ItemId;
import com.richodemus.reader.dto.UserId;
import com.richodemus.reader.dto.Username;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BackendTest
{
	private static final Username NON_EXISTING_USER = new Username("non_existing_user");
	private static final Item ITEM_THAT_SHOULD_BE_READ = new Item(new ItemId("item-id-1"), "item-title-1", "item-desc-1", LocalDateTime.ofEpochSecond(100L, 0, ZoneOffset.UTC), LocalDateTime.now(), Duration.ZERO, 0L);
	private static final Item ITEM_TO_MARK_AS_READ = new Item(new ItemId("item-id-2"), "item-title-2", "item-desc-2", LocalDateTime.ofEpochSecond(200L, 0, ZoneOffset.UTC), LocalDateTime.now(), Duration.ZERO, 0L);
	private static final Feed FEED_1 = new Feed(
			new FeedId("existing_feed_id"),
			new FeedName("name"),
			Arrays.asList(
					ITEM_THAT_SHOULD_BE_READ,
					ITEM_TO_MARK_AS_READ,
					new Item(new ItemId("item-id-3"), "item-title-3", "item-desc-3", LocalDateTime.ofEpochSecond(300L, 0, ZoneOffset.UTC), LocalDateTime.now(), Duration.ZERO, 0L),
					new Item(new ItemId("item-id-4"), "item-title-4", "item-desc-4", LocalDateTime.ofEpochSecond(400L, 0, ZoneOffset.UTC), LocalDateTime.now(), Duration.ZERO, 0L)
			), 0L);
	private static final Feed FEED_2 = new Feed(
			new FeedId("feed_2"),
			new FeedName("name"),
			Collections.singletonList(new Item(new ItemId("feed2-item1"), "title", "desc", LocalDateTime.ofEpochSecond(100L, 0, ZoneOffset.UTC), LocalDateTime.now(), Duration.ZERO, 0L)), 0L);


	private static final User EXISTING_USER = new User(new UserId("id"), new Username("existing_user"), 0L, ImmutableMap.of(FEED_1.getId(), Sets.newHashSet(ITEM_THAT_SHOULD_BE_READ.getId()), FEED_2.getId(), new HashSet<>()), new ArrayList<>());

	private Backend target;
	private SubscriptionRepository subscriptionRepository;
	private YoutubeFeedService feedService;

	@Before
	public void setUp() throws Exception
	{
		feedService = mock(YoutubeFeedService.class);
		subscriptionRepository = mock(SubscriptionRepository.class);
		target = new Backend(subscriptionRepository, feedService);


		when(subscriptionRepository.find(EXISTING_USER.getName())).thenReturn(EXISTING_USER);
		when(feedService.getChannel(FEED_1.getId())).thenReturn(Optional.of(FEED_1));
		when(feedService.getChannel(FEED_2.getId())).thenReturn(Optional.of(FEED_2));
	}

	@Test
	public void testAddFeed() throws Exception
	{
		final FeedUrl url = new FeedUrl("http://asd.com");
		final FeedId id = new FeedId("id");
		when(feedService.getFeedId(eq(url))).thenReturn(id);

		target.addFeed(EXISTING_USER.getName(), url);

		verify(feedService).registerChannel(eq(id));
		verify(subscriptionRepository).subscribe(EXISTING_USER.id, id);
	}

	@Test
	public void getFeedShouldReturnFeed() throws Exception
	{
		final List<com.richo.reader.backend.model.Item> unwatchedItems = FEED_1.getItems()
				.stream()
				.filter(i -> !EXISTING_USER.isRead(FEED_1.getId(), i.getId()))
				.map(i -> new com.richo.reader.backend.model.Item(i.getId(), i.getTitle(), i.getDescription(), i.getUploadDate().toString(), "https://youtube.com/watch?v=" + i.getId(), i.getDuration(), i.getViews()))
				.collect(toList());
		final com.richo.reader.backend.model.Feed expected = new com.richo.reader.backend.model.Feed(FEED_1.getId(), FEED_1.getName(), unwatchedItems);
		final Optional<com.richo.reader.backend.model.Feed> result = target.getFeed(EXISTING_USER.getName(), FEED_1.getId());

		assertThat(result.get()).isEqualTo(expected);
	}

	@Test
	public void getFeedsShouldReturnSubscribedFeeds() throws Exception
	{
		final List<FeedId> expected = Stream.of(FEED_1, FEED_2)
				.map(Feed::getId)
				.collect(toList());

		final List<FeedWithoutItems> result = target.getAllFeedsWithoutItems(EXISTING_USER.getName());

		assertThat(result).extracting(FeedWithoutItems::getId).isEqualTo(expected);
	}

	@Test
	public void shouldContainCorrectAmountOfUnreadItems() throws Exception
	{
		final List<FeedWithoutItems> result = target.getAllFeedsWithoutItems(EXISTING_USER.getName());

		assertThat(result).extracting(FeedWithoutItems::getNumberOfAvailableItems).containsOnly(3, 1);
	}

	@Test(expected = NoSuchUserException.class)
	public void getFeedShouldThrowNoSuchUserExceptionIfUserDoesntExist() throws Exception
	{
		when(subscriptionRepository.find(NON_EXISTING_USER)).thenThrow(new NoSuchUserException(""));
		target.getFeed(NON_EXISTING_USER, FEED_1.getId());
	}

	@Test(expected = NoSuchUserException.class)
	public void getAllFeedsWithoutItemsShouldThrowNoSuchUserExceptionIfUserDoesntExist() throws Exception
	{
		when(subscriptionRepository.find(NON_EXISTING_USER)).thenThrow(new NoSuchUserException(""));
		target.getAllFeedsWithoutItems(NON_EXISTING_USER);
	}

	@Test
	public void shouldMarkItemAsRead() throws Exception
	{
		target.markAsRead(EXISTING_USER.getName(), FEED_1.getId(), ITEM_TO_MARK_AS_READ.getId());
		verify(subscriptionRepository).markAsRead(EXISTING_USER.id, FEED_1.getId(), ITEM_TO_MARK_AS_READ.getId());
	}

	@Test
	public void shouldMarkItemAsUnread() throws Exception
	{
		target.markAsUnread(EXISTING_USER.getName(), FEED_1.getId(), ITEM_TO_MARK_AS_READ.getId());
		verify(subscriptionRepository).markAsUnread(EXISTING_USER.id, FEED_1.getId(), ITEM_TO_MARK_AS_READ.getId());
	}

	@Test
	public void markOlderItemsAsUnreadShouldLeadToOlderItemsNotBeingReturned() throws Exception
	{
		final ItemId id = new ItemId("item-id-4");
		target.markOlderItemsAsRead(EXISTING_USER.getName(), FEED_1.getId(), id);

		verify(subscriptionRepository).markAsRead(EXISTING_USER.id, FEED_1.getId(), new ItemId("item-id-1"));
		verify(subscriptionRepository).markAsRead(EXISTING_USER.id, FEED_1.getId(), new ItemId("item-id-2"));
		verify(subscriptionRepository).markAsRead(EXISTING_USER.id, FEED_1.getId(), new ItemId("item-id-3"));
		verify(subscriptionRepository, never()).markAsRead(EXISTING_USER.id, FEED_1.getId(), new ItemId("item-id-4"));
	}
}
