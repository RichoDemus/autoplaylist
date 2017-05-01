package com.richo.reader.backend;

import com.richo.reader.backend.exception.ItemNotInFeedException;
import com.richo.reader.backend.exception.NoSuchChannelException;
import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.exception.UserNotSubscribedToThatChannelException;
import com.richo.reader.backend.feed.FeedRepository;
import com.richo.reader.backend.model.Feed;
import com.richo.reader.backend.model.FeedWithoutItems;
import com.richo.reader.backend.model.Item;
import com.richo.reader.backend.subscription.SubscriptionRepository;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedUrl;
import com.richodemus.reader.dto.ItemId;
import com.richodemus.reader.dto.Username;
import com.richodemus.reader.user_service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class Backend
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final SubscriptionRepository subscriptionRepository;
	private final FeedRepository feedRepository;
	private final UserService userService;

	@Inject
	public Backend(final SubscriptionRepository subscriptionRepository,
				   FeedRepository feedRepository,
				   UserService userService)
	{
		this.subscriptionRepository = subscriptionRepository;
		this.feedRepository = feedRepository;
		this.userService = userService;
	}

	public Optional<com.richo.reader.backend.model.Feed> getFeed(Username username, FeedId feedId)
	{
		logger.debug("Getting feed {} for user {}", feedId, username);
		final com.richodemus.reader.user_service.User user = getUser(username);
		final Optional<Feed> feed = feedRepository.getFeed(feedId);
		if (!feed.isPresent())
		{
			logger.warn("No such feed {}", feedId);
			return Optional.empty();
		}

		final List<ItemId> watchedItems = subscriptionRepository.get(user.getId(), feedId);
		final List<Item> items = feed.get().getItems().stream().filter(item -> !watchedItems.contains(item.getId())).collect(toList());

		return Optional.of(new Feed(feedId, feed.get().getName(), items));
	}

	public List<FeedWithoutItems> getAllFeedsWithoutItems(Username username)
	{
		logger.debug("Getting all feeds for user {}", username);
		final com.richodemus.reader.user_service.User user = getUser(username);

		return subscriptionRepository.get(user.getId()).stream()
				.map(feedWithoutItems ->
				{
					final List<ItemId> watchedItems = feedWithoutItems.getItems().stream().map(Item::getId).collect(toList());
					final Feed feed = feedRepository.getFeed(feedWithoutItems.getId()).get();
					final List<Item> items = feed.getItems().stream().filter(item -> !watchedItems.contains(item.getId())).collect(toList());

					return new FeedWithoutItems(feedWithoutItems.getId(), feed.getName(), items.size());
				})
				.collect(toList());
	}

	public void addFeed(final Username username, final FeedUrl feedUrl) throws NoSuchChannelException, NoSuchUserException
	{
		logger.info("Add feed: {} for user {}", feedUrl, username);

		final FeedId feedId = feedRepository.getFeedId(feedUrl);
		final com.richodemus.reader.user_service.User user = getUser(username);

		//Todo its now possible to add feeds that doesnt exist...
		feedRepository.registerChannel(feedId);
		subscriptionRepository.subscribe(user.getId(), feedId);
	}

	public void markAsRead(final Username username, final FeedId feedId, final ItemId itemId) throws NoSuchUserException, UserNotSubscribedToThatChannelException
	{
		logger.info("Marking item {} in feed {} for user {} as read", itemId, feedId, username);
		final com.richodemus.reader.user_service.User user = getUser(username);
		subscriptionRepository.markAsRead(user.getId(), feedId, itemId);
	}

	public void markAsUnread(final Username username, final FeedId feedId, ItemId itemId) throws NoSuchUserException
	{
		logger.info("Marking item {} in feed {} for user {} as unread", itemId, feedId, username);
		final com.richodemus.reader.user_service.User user = getUser(username);
		subscriptionRepository.markAsUnread(user.getId(), feedId, itemId);
	}

	public void markOlderItemsAsRead(final Username username, final FeedId feedId, final ItemId itemId) throws NoSuchChannelException, ItemNotInFeedException, UserNotSubscribedToThatChannelException
	{
		logger.info("Marking items older than {} in feed {} for user {} as read", itemId, feedId, username);
		final com.richodemus.reader.user_service.User user = getUser(username);


		final Feed feed = feedRepository.getFeed(feedId).orElseThrow(() ->
		{
			logger.error("No such channel: {}", feedId);
			return new NoSuchChannelException("No such channel: " + feedId);
		});

		final Item targetItem = feed.getItems().stream()
				.filter(item -> item.getId().equals(itemId))
				.findAny()
				.orElseThrow(() -> new ItemNotInFeedException("Item " + itemId + " is not in feed " + feedId));

		feed.getItems().stream()
				.filter(item -> item.isBefore(targetItem))
				.map(Item::getId)
				.forEach(id -> subscriptionRepository.markAsRead(user.getId(), feedId, id));
	}

	private com.richodemus.reader.user_service.User getUser(Username username)
	{
		final com.richodemus.reader.user_service.User user = userService.find(username);
		if (user == null)
		{
			throw new NoSuchUserException("No such user: " + username);
		}
		return user;
	}
}
