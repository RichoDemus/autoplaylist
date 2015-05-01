package com.richo.reader.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Backend
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public void getFeeds(final String username)
	{
		logger.info("Getting feeds for user {}", username);
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
