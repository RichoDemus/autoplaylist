package com.richodemus.reader.backend.subscription;

import com.richodemus.reader.backend.model.Feed;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.ItemId;
import com.richodemus.reader.dto.UserId;

import java.util.List;

public interface SubscriptionRepository {
    List<Feed> get(final UserId userId);

    List<ItemId> get(final UserId userId, final FeedId feedId);

    void subscribe(final UserId userId, final FeedId feedId);

    void markAsRead(final UserId userId, final FeedId feedId, final ItemId itemId);

    void markAsUnread(UserId userId, FeedId feedId, ItemId itemId);
}
