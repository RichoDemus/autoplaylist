package com.richo.reader.backend;

import com.richo.reader.backend.exception.NoSuchChannelException;
import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.exception.UserNotSubscribedToThatChannelException;
import com.richo.reader.backend.model.User;
import com.richo.reader.backend.user.UserService;
import com.richo.reader.youtube_feed_service.Feed;
import com.richo.reader.youtube_feed_service.YoutubeFeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Backend
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final UserService userService;
	private final YoutubeFeedService feedService;

	@Inject
	public Backend(final UserService userService, YoutubeFeedService feedService)
	{
		this.userService = userService;
		this.feedService = feedService;
	}

	public Optional<com.richo.reader.model.Feed> getFeed(String username, String feedId)
	{
		logger.debug("Getting feed {} for user {}", feedId, username);

		final User user = userService.get(username);
		if(!user.getFeeds().containsKey(feedId))
		{
			logger.debug("{} is not subscrbed to feed {}", username, feedId);
			return Optional.empty();
		}

		final Feed feed = feedService.getChannel(feedId).orElseThrow(() -> new NoSuchChannelException("Couldn find feed " + feedId));

		final List<com.richo.reader.model.Item> unwatchedItems = feed.getItems().stream()
				.filter(i -> !user.getFeeds().get(feedId).contains(i.getId()))
				.map(i -> new com.richo.reader.model.Item(i.getId(), i.getTitle(), i.getDescription(), i.getUploadDate().toString(), "fix url in Backend.java"))
				.collect(Collectors.toList());

		return Optional.of(new com.richo.reader.model.Feed(feed.getId(), feed.getId(), unwatchedItems));
	}

/*	public Set<Feed> getFeeds(final String username) throws NoSuchUserException
	{
		logger.info("Getting feeds for user {}", username);

		final User user = userService.get(username);

		return user.getFeeds().keySet().stream()
				.map(this::feedIdToYoutubeChannel)
				.map(channel -> youtubeChannelToFeed(channel, user))
				.collect(Collectors.toSet());
	}*/

/*	private Feed youtubeChannelToFeed(YoutubeChannel channel, User user)
	{
		final String id = channel.getName();
		final Feed feed = new Feed(id, id);
		feed.addNewItems(channel.getVideos()
				.stream()
				.filter(video -> !user.isRead(channel.getName(), video.getVideoId()))
				.map(this::videoToItem)
				.collect(Collectors.toSet()));
		return feed;
	}

	private Item videoToItem(YoutubeVideo video)
	{
		return new Item(video.getVideoId(), video.getTitle(), video.getDescription(), video.getUrl(), video.getUploadDate());
	}*/

	public void addFeed(final String username, final String feedName) throws NoSuchChannelException, NoSuchUserException
	{
		logger.info("Add feed: {} for user {}", feedName, username);

		final User user = userService.get(username);

		//Todo its now possible to add feeds that doesnt exist...
		user.addFeed(feedName);
		userService.update(user);
	}

	public void markAsRead(final String username, final String feedId, String itemId) throws NoSuchUserException, UserNotSubscribedToThatChannelException
	{
		logger.info("Marking item {} in feed {} for user {} as read", itemId, feedId, username);
		final User user = userService.get(username);
		user.markAsRead(feedId, itemId);
		userService.update(user);
	}

	public void markAsUnread(final String username, final String feedId, String itemId) throws NoSuchUserException
	{
		logger.info("Marking item {} in feed {} for user {} as unread", itemId, feedId, username);
		final User user = userService.get(username);
		user.markAsUnRead(feedId, itemId);
		userService.update(user);
	}

/*	public void markOlderItemsAsRead(final String username, final String feedId, final String itemId) throws NoSuchChannelException, ItemNotInFeedException, UserNotSubscribedToThatChannelException
	{
		logger.info("Marking items older than {} in feed {} for user {} as read", itemId, feedId, username);
		final User user = userService.get(username);
		final Feed feed = feedService.getChannel(feedId).orElseThrow(() ->
		{
			logger.error("No such channel: {}", feedId);
			return new NoSuchChannelException("No such channel: " + feedId);
		});

		final Item targetItem = feed.getItems().stream()
				.filter(item -> item.getVideoId().equals(itemId))
				.findAny()
				.orElseThrow(() -> new ItemNotInFeedException("Item " + itemId + " is not in feed " + feedId));

		feed.getItems().stream()
				.filter(item -> item.getUploadDate().isBefore(targetItem.getUploadDate()))
				.map(Item::getVideoId)
				.forEach(id -> user.markAsRead(feedId, id));

		userService.update(user);
	}*/
}
