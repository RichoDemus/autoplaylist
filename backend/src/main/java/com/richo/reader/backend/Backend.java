package com.richo.reader.backend;

import com.google.common.collect.Sets;
import com.richo.reader.backend.model.Feed;
import com.richo.reader.backend.user.User;
import com.richo.reader.backend.user.UserService;
import com.richo.reader.backend.youtube.YoutubeChannelService;
import com.richo.reader.backend.youtube.model.YoutubeChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
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
		final List<String> subscribedFeeds = user.getSubscribedFeeds().stream().map(Feed::getName).collect(Collectors.toList());

		final List<YoutubeChannel> youtubeChannels = subscribedFeeds.stream().map(youtubeChannelService::getChannelByName).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

		youtubeChannels.forEach(user::updateChannel);

		return user.getSubscribedFeeds();
	}

	public void markAsRead(final String username, final String itemId)
	{
		logger.info("Marking item {} for user {} as read", itemId, username);
	}

	public void markAsUnread(final String username, final String itemId)
	{
		logger.info("Marking item {} for user {} as unread", itemId, username);
	}
}
