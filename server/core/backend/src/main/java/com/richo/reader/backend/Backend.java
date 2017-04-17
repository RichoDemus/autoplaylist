package com.richo.reader.backend;

import com.richo.reader.backend.exception.ItemNotInFeedException;
import com.richo.reader.backend.exception.NoSuchChannelException;
import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.exception.UserNotSubscribedToThatChannelException;
import com.richo.reader.backend.model.FeedWithoutItems;
import com.richo.reader.backend.model.User;
import com.richo.reader.backend.user.SubscriptionRepository;
import com.richo.reader.youtube_feed_service.Feed;
import com.richo.reader.youtube_feed_service.Item;
import com.richo.reader.youtube_feed_service.YoutubeFeedService;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedUrl;
import com.richodemus.reader.dto.ItemId;
import com.richodemus.reader.dto.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Backend
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final SubscriptionRepository subscriptionRepository;
	private final YoutubeFeedService feedService;

	@Inject
	public Backend(final SubscriptionRepository subscriptionRepository, YoutubeFeedService feedService)
	{
		this.subscriptionRepository = subscriptionRepository;
		this.feedService = feedService;
	}

	public Optional<com.richo.reader.backend.model.Feed> getFeed(UserId username, FeedId feedId)
	{
		logger.debug("Getting feed {} for user {}", feedId, username);

		final User user = subscriptionRepository.get(username);
		if (!user.getFeeds().containsKey(feedId))
		{
			logger.warn("{} is not subscrbed to feed {}", username, feedId);
		}

		final Feed feed = feedService.getChannel(feedId).orElseThrow(() -> new NoSuchChannelException("Couldn find feed " + feedId));
		logger.debug("Found feed: {}", feed);

		final List<com.richo.reader.backend.model.Item> unwatchedItems = feed.getItems().stream()
				.filter(i -> !user.getFeeds().get(feedId).contains(i.getId()))
				.map(i -> new com.richo.reader.backend.model.Item(i.getId(), i.getTitle(), i.getDescription(), i.getUploadDate().toString(), "https://youtube.com/watch?v=" + i.getId(), i.getDuration(), i.getViews()))
				.collect(Collectors.toList());

		return Optional.of(new com.richo.reader.backend.model.Feed(feed.getId(), feed.getName(), unwatchedItems));
	}

	public List<FeedWithoutItems> getAllFeedsWithoutItems(UserId username)
	{
		logger.debug("Getting all feeds for user {}", username);

		final User user = subscriptionRepository.get(username);

		return user.getFeeds().keySet().stream()
				.map(feedService::getChannel)
				.flatMap(this::toStream)
				.map(f -> new FeedWithoutItems(f.getId(), f.getName(), calculateNumberOfItems(user, f)))
				.collect(Collectors.toList());
	}

	private Stream<Feed> toStream(Optional<Feed> o)
	{
		return o.isPresent() ? Stream.of(o.get()) : Stream.empty();
	}

	//todo make more functional
	private int calculateNumberOfItems(final User user, final Feed feed)
	{
		if (!user.getFeeds().containsKey(feed.getId()))
		{
			return 0;
		}

		final Set<ItemId> readItems = user.getFeeds().get(feed.getId());

		final List<ItemId> allItemIds = feed.getItems().stream().map(Item::getId).collect(Collectors.toList());

		allItemIds.removeAll(readItems);

		return allItemIds.size();
	}

	public void addFeed(final UserId username, final FeedUrl feedUrl) throws NoSuchChannelException, NoSuchUserException
	{
		logger.info("Add feed: {} for user {}", feedUrl, username);

		final FeedId feedId = feedService.getFeedId(feedUrl);
		final User user = subscriptionRepository.get(username);

		//Todo its now possible to add feeds that doesnt exist...
		user.addFeed(feedId);
		subscriptionRepository.update(user);
		feedService.registerChannel(feedId);
	}

	public void markAsRead(final UserId username, final FeedId feedId, final ItemId itemId) throws NoSuchUserException, UserNotSubscribedToThatChannelException
	{
		logger.info("Marking item {} in feed {} for user {} as read", itemId, feedId, username);
		final User user = subscriptionRepository.get(username);
		user.markAsRead(feedId, itemId);
		subscriptionRepository.update(user);
	}

	public void markAsUnread(final UserId username, final FeedId feedId, ItemId itemId) throws NoSuchUserException
	{
		logger.info("Marking item {} in feed {} for user {} as unread", itemId, feedId, username);
		final User user = subscriptionRepository.get(username);
		user.markAsUnRead(feedId, itemId);
		subscriptionRepository.update(user);
	}

	public void markOlderItemsAsRead(final UserId username, final FeedId feedId, final ItemId itemId) throws NoSuchChannelException, ItemNotInFeedException, UserNotSubscribedToThatChannelException
	{
		logger.info("Marking items older than {} in feed {} for user {} as read", itemId, feedId, username);
		final User user = subscriptionRepository.get(username);
		final Feed feed = feedService.getChannel(feedId).orElseThrow(() ->
		{
			logger.error("No such channel: {}", feedId);
			return new NoSuchChannelException("No such channel: " + feedId);
		});

		final Item targetItem = feed.getItems().stream()
				.filter(item -> item.getId().equals(itemId))
				.findAny()
				.orElseThrow(() -> new ItemNotInFeedException("Item " + itemId + " is not in feed " + feedId));

		feed.getItems().stream()
				.filter(item -> item.getUploadDate().isBefore(targetItem.getUploadDate()))
				.map(Item::getId)
				.forEach(id -> user.markAsRead(feedId, id));

		subscriptionRepository.update(user);
	}
}
