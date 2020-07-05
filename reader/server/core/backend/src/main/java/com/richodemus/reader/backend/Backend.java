package com.richodemus.reader.backend;

import com.richodemus.reader.backend.exception.ItemNotInFeedException;
import com.richodemus.reader.backend.exception.NoSuchChannelException;
import com.richodemus.reader.backend.exception.NoSuchUserException;
import com.richodemus.reader.backend.exception.UserNotSubscribedToThatChannelException;
import com.richodemus.reader.backend.feed.FeedRepository;
import com.richodemus.reader.backend.model.Feed;
import com.richodemus.reader.backend.model.FeedWithoutItems;
import com.richodemus.reader.backend.model.Item;
import com.richodemus.reader.backend.subscription.SubscriptionRepository;
import com.richodemus.reader.backend.user.UserRepository;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedName;
import com.richodemus.reader.dto.FeedUrl;
import com.richodemus.reader.dto.ItemId;
import com.richodemus.reader.dto.UserId;
import com.richodemus.reader.dto.Username;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class Backend
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final SubscriptionRepository subscriptionRepository;
	private final FeedRepository feedRepository;
	private final UserRepository userRepository;

	public Backend(final SubscriptionRepository subscriptionRepository,
				   final FeedRepository feedRepository,
				   final UserRepository userRepository)
	{
		this.subscriptionRepository = subscriptionRepository;
		this.feedRepository = feedRepository;
		this.userRepository = userRepository;

	}

	public Optional<Feed> getFeed(Username username, FeedId feedId)
	{
		logger.debug("Getting feed {} for user {}", feedId, username);
		final UserId userId = userRepository.getUserId(username);
		final Optional<Feed> feed = feedRepository.getFeed(feedId);
		if (!feed.isPresent())
		{
			logger.warn("No such feed {}", feedId);
			return Optional.empty();
		}

		final List<ItemId> watchedItems = subscriptionRepository.get(userId, feedId);
		final List<Item> items = feed.get().getItems().stream().filter(item -> !watchedItems.contains(item.getId())).collect(toList());

		return Optional.of(new Feed(feedId, feed.get().getName(), items));
	}

	public List<FeedWithoutItems> getAllFeedsWithoutItems(Username username)
	{
		logger.debug("Getting all feeds for user {}", username);
		final UserId userId = userRepository.getUserId(username);

		final List<Feed> feeds = subscriptionRepository.get(userId);

		final Map<FeedId, Feed> feedIds = getAllFeedsForUser(feeds);

		return mergeFeeds(feeds, feedIds);

	}

	private Map<FeedId, Feed> getAllFeedsForUser(List<Feed> feeds)
	{
		return feeds.stream()
				.collect(toMap(Feed::getId, feed ->
				{
					final Optional<Feed> feedRepositoryFeed = feedRepository.getFeed(feed.getId());
					return feedRepositoryFeed.orElseGet(() -> new Feed(feed.getId(), new FeedName("UNKNOWN_FEED"), emptyList()));
				}));
	}

	private List<FeedWithoutItems> mergeFeeds(List<Feed> feeds, Map<FeedId, Feed> feedsWithItems)
	{
		List<FeedWithoutItems> results = new ArrayList<>();

		for (Feed feed : feeds)
		{
			final List<Item> watchedItems = new ArrayList<>(feed.getItems());
			final Feed repoFeed = feedsWithItems.get(feed.getId());
			final List<Item> itemIds = repoFeed.getItems();

			int numberOfUnwatchedItems = itemIds.size();

			for (Item item : itemIds)
			{
				for (int i = 0; i < watchedItems.size(); i++)
				{

					if (item.getId().equals(watchedItems.get(i).getId()))
					{
						numberOfUnwatchedItems--;
						watchedItems.remove(i);
						break;
					}
				}
			}

			results.add(new FeedWithoutItems(feed.getId(), repoFeed.getName(), numberOfUnwatchedItems));
		}

		return results;
	}

	public void addFeed(final Username username, final FeedUrl feedUrl) throws NoSuchChannelException, NoSuchUserException
	{
		logger.info("Add feed: {} for user {}", feedUrl, username);

		final FeedId feedId = feedRepository.getFeedId(feedUrl);
		final UserId userId = userRepository.getUserId(username);

		//Todo its now possible to add feeds that doesnt exist...
		subscriptionRepository.subscribe(userId, feedId);
	}

	public void markAsRead(final Username username, final FeedId feedId, final ItemId itemId) throws NoSuchUserException, UserNotSubscribedToThatChannelException
	{
		logger.info("Marking item {} in feed {} for user {} as read", itemId, feedId, username);
		final UserId userId = userRepository.getUserId(username);
		subscriptionRepository.markAsRead(userId, feedId, itemId);
	}

	public void markAsUnread(final Username username, final FeedId feedId, ItemId itemId) throws NoSuchUserException
	{
		logger.info("Marking item {} in feed {} for user {} as unread", itemId, feedId, username);
		final UserId userId = userRepository.getUserId(username);
		subscriptionRepository.markAsUnread(userId, feedId, itemId);
	}

	public void markOlderItemsAsRead(final Username username, final FeedId feedId, final ItemId itemId) throws NoSuchChannelException, ItemNotInFeedException, UserNotSubscribedToThatChannelException
	{
		logger.info("Marking items older than {} in feed {} for user {} as read", itemId, feedId, username);
		final UserId userId = userRepository.getUserId(username);

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
				.forEach(id -> subscriptionRepository.markAsRead(userId, feedId, id));
	}
}
