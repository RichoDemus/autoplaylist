package com.richo.reader.subscription_service

import com.codahale.metrics.Gauge
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.MetricRegistry.name
import com.richodemus.reader.common.kafka_adapter.EventStore
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.events.CreateUser
import com.richodemus.reader.events.Event
import com.richodemus.reader.events.UserSubscribedToFeed
import com.richodemus.reader.events.UserUnwatchedItem
import com.richodemus.reader.events.UserWatchedItem
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionService @Inject internal constructor(val eventStore: EventStore, registry: MetricRegistry) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var processedEvents = AtomicLong()

    private var users = emptyMap<UserId, User>()

    init {
        eventStore.consume { event ->
            if (event is CreateUser) {
                warnIfUserAlreadyExists(event.userId)
                users = users.plus(Pair(event.userId, User(event.userId)))
            }

            warnIfDoesntExist(event)
            users = users.mapValues { it.value.process(event) }
            processedEvents.incrementAndGet()
        }
        registry.register(name(SubscriptionService::class.java, "processed-events"),
                Gauge<Long> { processedEvents.get() })
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
