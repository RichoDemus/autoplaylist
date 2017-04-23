package com.richo.reader.backend.subscription;

import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.model.User;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.ItemId;
import com.richodemus.reader.dto.UserId;
import com.richodemus.reader.dto.Username;

public interface SubscriptionRepository
{
	User find(Username username) throws NoSuchUserException;

	@Deprecated
	void update(User user);

	void subscribe(final UserId userId, final FeedId feedId);

	void markAsRead(final UserId userId, final FeedId feedId, final ItemId itemId);

	void markAsUnread(UserId userId, FeedId feedId, ItemId itemId);
}
