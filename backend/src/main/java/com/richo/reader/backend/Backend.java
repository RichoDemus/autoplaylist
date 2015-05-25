package com.richo.reader.backend;

import com.google.common.collect.Sets;
import com.richo.reader.backend.exception.NoSuchChannelException;
import com.richo.reader.backend.model.Feed;
import com.richo.reader.backend.model.Item;
import com.richo.reader.backend.model.User;
import com.richo.reader.backend.user.UserService;
import com.richo.reader.backend.youtube.YoutubeChannelService;
import com.richo.reader.backend.youtube.model.YoutubeChannel;
import com.richo.reader.backend.youtube.model.YoutubeVideo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Backend
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final UserService userService;
	private final YoutubeChannelService youtubeChannelService;

	@Inject
	public Backend(final UserService userService, YoutubeChannelService youtubeChannelService)
	{
		this.userService = userService;
		this.youtubeChannelService = youtubeChannelService;
	}

	public Set<Feed> getFeeds(final String username)
	{
		logger.info("Getting feeds for user {}", username);

		final Optional<User> userOptional = userService.get(username);
		if (!userOptional.isPresent())
		{
			logger.error("No such user: {}", username);
			return Sets.newHashSet();
		}

		final User user = userOptional.get();

		return user.getFeeds().stream().map(this::feedIdToYoutubeChannel).map(this::youtubeChannelToItem).collect(Collectors.toSet());
	}

	private YoutubeChannel feedIdToYoutubeChannel(String name)
	{
		return youtubeChannelService.getChannelByName(name).orElseGet(() ->
		{
			logger.error("No such feed: {}", name);
			return null;
		});
	}

	private Feed youtubeChannelToItem(YoutubeChannel channel)
	{
		final String id = channel.getName();
		final Feed feed = new Feed(id, id);
		feed.addNewItems(channel.getVideos().stream().map(this::videoToItem).collect(Collectors.toSet()));
		return feed;
	}

	private Item videoToItem(YoutubeVideo video)
	{
		return new Item(video.getVideoId(), video.getTitle(), video.getDescription(), video.getUrl(), video.getUploadDate());
	}

	public void addFeed(final String username, final String feedName) throws NoSuchChannelException
	{
		logger.info("Add feed: {} for user {}", feedName, username);

		final Optional<User> userOptional = userService.get(username);
		if (!userOptional.isPresent())
		{
			logger.error("No such user: {}", username);
		}

		final User user = userOptional.get();

		final YoutubeChannel channelByName = youtubeChannelService.getChannelByName(feedName).orElseThrow(() ->
		{
			logger.error("No such channel: {}", feedName);
			return new NoSuchChannelException("No such channel: " + feedName);
		});

		user.addFeed(channelByName.getName());
		userService.update(user);
	}

	public void markAsRead(final String username, final String feedId, String itemId)
	{
		logger.info("Marking item {} in feed {} for user {} as read", itemId, feedId, username);
	}

	public void markAsUnread(final String username, final String feedId, String itemId)
	{
		logger.info("Marking item {} in feed {} for user {} as unread", itemId, feedId, username);
	}
}
