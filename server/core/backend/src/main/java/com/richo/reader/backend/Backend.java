package com.richo.reader.backend;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
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
import com.richodemus.reader.dto.FeedName;
import com.richodemus.reader.dto.FeedUrl;
import com.richodemus.reader.dto.ItemId;
import com.richodemus.reader.dto.Username;
import com.richodemus.reader.user_service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.codahale.metrics.MetricRegistry.name;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class Backend
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final SubscriptionRepository subscriptionRepository;
	private final FeedRepository feedRepository;
	private final UserService userService;
	private Timer getAllFeedsWithoutItemsTimer;
	private Timer getFeedTimer;
	private Timer getFeedsForUserTimer;
	private Timer mergeFeedsTimer;

	@Inject
	public Backend(final SubscriptionRepository subscriptionRepository,
				   FeedRepository feedRepository,
				   UserService userService,
				   final MetricRegistry registry)
	{
		this.subscriptionRepository = subscriptionRepository;
		this.feedRepository = feedRepository;
		this.userService = userService;

		getAllFeedsWithoutItemsTimer = registry.timer(name(Backend.class, "getAllFeedsWithoutItems"));
		getFeedTimer = registry.timer(name(Backend.class, "getFeed"));
		getFeedsForUserTimer = registry.timer(name(Backend.class, "getFeedsForUser"));
		mergeFeedsTimer = registry.timer(name(Backend.class, "mergeFeeds"));
	}

	public Optional<Feed> getFeed(Username username, FeedId feedId)
	{
		final Timer.Context context = getFeedTimer.time();
		try
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
		finally
		{
			context.stop();
		}
	}

	public List<FeedWithoutItems> getAllFeedsWithoutItems(Username username)
	{
		final Timer.Context context = getAllFeedsWithoutItemsTimer.time();
		try
		{
			logger.debug("Getting all feeds for user {}", username);
			final com.richodemus.reader.user_service.User user = getUser(username);

			final List<Feed> feeds = subscriptionRepository.get(user.getId());

			final Map<FeedId, Feed> feedIds = getAllFeedsForUser(feeds);

			return mergeFeeds(feeds, feedIds);

		}
		finally
		{
			context.stop();
		}
	}

	private Map<FeedId, Feed> getAllFeedsForUser(List<Feed> feeds)
	{
		final Timer.Context context = getFeedsForUserTimer.time();
		try
		{
			return feeds.stream()
					.collect(toMap(Feed::getId, feed ->
					{
						final Optional<Feed> feedRepositoryFeed = feedRepository.getFeed(feed.getId());
						return feedRepositoryFeed.orElseGet(() -> new Feed(feed.getId(), new FeedName("404: " + feed.getId()), emptyList()));
					}));
		}
		finally
		{
			context.stop();
		}
	}

	private List<FeedWithoutItems> mergeFeeds(List<Feed> feeds, Map<FeedId, Feed> feedsWithItems)
	{
		final Timer.Context context = mergeFeedsTimer.time();
		try
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
		finally
		{
			context.stop();
		}
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
