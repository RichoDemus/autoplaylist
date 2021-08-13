package com.richodemus.reader.backend.subscription

import com.richodemus.reader.backend.exception.NoSuchUserException
import com.richodemus.reader.backend.model.Feed
import com.richodemus.reader.backend.model.Item
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.subscription_service.SubscriptionService
import org.springframework.stereotype.Repository
import java.time.Duration
import java.util.stream.Collectors

@Repository
class SubscriptionServicePort internal constructor(private val subscriptionService: SubscriptionService) : SubscriptionRepository {
    override fun get(userId: UserId): List<Feed> {
        val (_, feeds) = subscriptionService.get(userId) ?: throw NoSuchUserException("No such user $userId")
        return feeds.stream()
                .map { (id, watchedItems1): com.richodemus.reader.subscription_service.Feed ->
                    val watchedItems = watchedItems1.stream().map { i: ItemId -> Item(i, "", "", "", "", Duration.ZERO, 0L) }.collect(Collectors.toList())
                    Feed(id, null, watchedItems)
                }
                .collect(Collectors.toList())
    }

    override fun get(userId: UserId, feedId: FeedId): List<ItemId> {
        val (_, feeds) = subscriptionService.get(userId) ?: throw NoSuchUserException("No such user $userId")
        return feeds.stream()
                .filter { (id): com.richodemus.reader.subscription_service.Feed -> id == feedId }
                .map(com.richodemus.reader.subscription_service.Feed::watchedItems)
                .findAny()
                .orElseThrow { IllegalStateException("User $userId not subscribed to $feedId") }
    }

    override fun subscribe(userId: UserId, feedId: FeedId) {
        subscriptionService.subscribe(userId, feedId)
    }

    override fun markAsRead(userId: UserId, feedId: FeedId, itemId: ItemId) {
        subscriptionService.markAsRead(userId, feedId, itemId)
    }

    override fun markAsUnread(userId: UserId, feedId: FeedId, itemId: ItemId) {
        subscriptionService.markAsUnread(userId, feedId, itemId)
    }

//    private fun convert2(feeds: Map<FeedId, List<ItemId>>): Map<FeedId, Set<ItemId>> {
//        return feeds.entries.stream().collect(Collectors.toMap({ (key, value) -> java.util.Map.Entry.key }) { (_, value): Map.Entry<FeedId, List<ItemId>> -> HashSet(value) })
//    }
}
