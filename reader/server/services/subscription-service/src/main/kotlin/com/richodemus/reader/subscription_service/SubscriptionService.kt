package com.richodemus.reader.subscription_service

import com.richodemus.reader.common.google_cloud_storage_adapter.EventStore
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.events_v2.Event
import com.richodemus.reader.events_v2.UserCreated
import com.richodemus.reader.events_v2.UserSubscribedToFeed
import com.richodemus.reader.events_v2.UserUnwatchedItem
import com.richodemus.reader.events_v2.UserWatchedItem
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicLong

@Service
class SubscriptionService(private val eventStore: EventStore) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var processedEvents = AtomicLong()

    private var users = emptyMap<UserId, User>()

    init {
        eventStore.consume { event ->
            if (event is UserCreated) {
                warnIfUserAlreadyExists(event.userId)
                users = users.plus(Pair(event.userId, User(event.userId)))
            }

            warnIfDoesntExist(event)
            users = users.mapValues { it.value.process(event) }
            processedEvents.incrementAndGet()
        }
    }

    fun get(userId: UserId) = users[userId]

    fun subscribe(userId: UserId, feedId: FeedId) {
        assertUserExists(userId) { "User $userId can't subscribe to feed $feedId: User does not exist" }
        eventStore.produce(UserSubscribedToFeed(userId, feedId))
    }

    fun markAsRead(userId: UserId, feedId: FeedId, itemId: ItemId) {
        assertUserExists(userId) { "User $userId can't mark item $itemId as read in feed $feedId: User does not exist" }
        assertUserSubscribedTo(userId, feedId)
        eventStore.produce(UserWatchedItem(userId, feedId, itemId))
    }

    fun markAsUnread(userId: UserId, feedId: FeedId, itemId: ItemId) {
        assertUserExists(userId) { "User $userId can't mark item $itemId as read in feed $feedId: User does not exist" }
        assertUserSubscribedTo(userId, feedId)
        eventStore.produce(UserUnwatchedItem(userId, feedId, itemId))
    }

    private fun assertUserExists(userId: UserId, msg: () -> String) {
        if (!exists(userId)) {
            throw IllegalStateException(msg.invoke())
        }
    }

    private fun assertUserSubscribedTo(userId: UserId, feedId: FeedId) {
        if (users[userId]?.feeds?.any { it.id == feedId } == false) {
            throw IllegalStateException("User $userId is not subscribed to $feedId")
        }
    }

    private fun warnIfUserAlreadyExists(userId: UserId) {
        if (exists(userId)) {
            logger.warn("User $userId already exists")
        }
    }

    private fun warnIfDoesntExist(event: Event) {
        when (event) {
            is UserSubscribedToFeed -> if (users.none { it.key == event.userId }) logger.warn("User ${event.userId} doesn't exist")
            is UserWatchedItem -> if (users.none { it.key == event.userId }) logger.warn("User ${event.userId} doesn't exist")
            is UserUnwatchedItem -> if (users.none { it.key == event.userId }) logger.warn("User ${event.userId} doesn't exist")
        }
    }

    private fun exists(id: UserId) = users.containsKey(id)
}
