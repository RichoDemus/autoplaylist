package com.richodemus.reader.backend.subscription

import com.richodemus.reader.backend.model.Feed
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.UserId

interface SubscriptionRepository {
    operator fun get(userId: UserId): List<Feed>
    operator fun get(userId: UserId, feedId: FeedId): List<ItemId>
    fun subscribe(userId: UserId, feedId: FeedId)
    fun markAsRead(userId: UserId, feedId: FeedId, itemId: ItemId)
    fun markAsUnread(userId: UserId, feedId: FeedId, itemId: ItemId)
}
