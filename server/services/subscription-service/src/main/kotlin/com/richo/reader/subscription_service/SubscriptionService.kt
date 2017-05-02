package com.richo.reader.subscription_service

import com.codahale.metrics.Gauge
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.MetricRegistry.name
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.events.CreateUser
import com.richodemus.reader.events.UserSubscribedToFeed
import com.richodemus.reader.events.UserUnwatchedItem
import com.richodemus.reader.events.UserWatchedItem
import io.reactivex.rxkotlin.subscribeBy
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionService @Inject internal constructor(private val fileSystemPersistence: FileSystemPersistence, val eventStore: EventStore, registry: MetricRegistry) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var page = AtomicLong()

    private val users = mutableMapOf<UserId, User>()

    init {
        eventStore.observe().subscribeBy(
                onNext = { event ->
                    when (event) {
                        is CreateUser -> add(event)
                        is UserSubscribedToFeed -> subscribe(event)
                        is UserWatchedItem -> watch(event)
                        is UserUnwatchedItem -> unwatch(event)
                        else -> logger.debug("Not handling event of type {}", event.type)
                    }
                    // todo actually read page from Chronicler
                    page.incrementAndGet()
                },
                onError = { logger.error("Subscription service event stream failure", it) },
                onComplete = { logger.info("Subscription service event stream closed") }
        )
        registry.register(name(SubscriptionService::class.java, "page"), Gauge<Long> { page.get() })
    }

    fun get(userId: UserId) = users[userId]

    fun subscribe(userId: UserId, feedId: FeedId) {
        assertUserExists(userId) { "User $userId can't subscribe to feed $feedId: User does not exist" }
        eventStore.add(UserSubscribedToFeed(userId, feedId))
    }

    fun markAsRead(userId: UserId, feedId: FeedId, itemId: ItemId) {
        assertUserExists(userId) { "User $userId can't mark item $itemId as read in feed $feedId: User does not exist" }
        assertUserSubscribedTo(userId, feedId)
        eventStore.add(UserWatchedItem(userId, feedId, itemId))
    }

    fun markAsUnread(userId: UserId, feedId: FeedId, itemId: ItemId) {
        assertUserExists(userId) { "User $userId can't mark item $itemId as read in feed $feedId: User does not exist" }
        assertUserSubscribedTo(userId, feedId)
        eventStore.add(UserUnwatchedItem(userId, feedId, itemId))
    }

    private fun add(event: CreateUser) {
        warnIfUserAlreadyExists(event.userId)
        users.put(event.userId, User(event.userId))
    }

    private fun subscribe(event: UserSubscribedToFeed) {
        warnIfDoesntExist(event.userId)
        users[event.userId]?.subscribe(event.feedId)
    }

    private fun watch(event: UserWatchedItem) {
        warnIfDoesntExist(event.userId)
        warnIfNotSubscribed(event.userId, event.feedId)
        users[event.userId]?.watch(event.feedId, event.itemId)
    }

    private fun unwatch(event: UserUnwatchedItem) {
        warnIfDoesntExist(event.userId)
        warnIfNotSubscribed(event.userId, event.feedId)
        users[event.userId]?.unWatch(event.feedId, event.itemId)
    }

    private fun assertUserExists(userId: UserId, msg: () -> String) {
        if (!exists(userId)) {
            throw IllegalStateException(msg.invoke())
        }
    }

    private fun assertUserSubscribedTo(userId: UserId, feedId: FeedId) {
        if (users[userId]?.feeds?.get(feedId) == null) {
            throw IllegalStateException("User $userId is not subscribed to $feedId")
        }
    }

    private fun warnIfUserAlreadyExists(userId: UserId) {
        if (exists(userId)) {
            logger.warn("User $userId already exists")
        }
    }

    private fun warnIfDoesntExist(userId: UserId) {
        if (!exists(userId)) {
            logger.warn("User $userId doesn't exist")
        }
    }

    private fun warnIfNotSubscribed(userId: UserId, feedId: FeedId) {
        if (users[userId]?.feeds?.get(feedId) == null) {
            logger.warn("User $userId is not subscribed to $feedId")
        }
    }

    private fun exists(id: UserId) = users.containsKey(id)
}
