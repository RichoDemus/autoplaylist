package com.richo.reader.subscription_service

import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import com.richodemus.reader.events.CreateUser
import com.richodemus.reader.events.UserSubscribedToFeed
import com.richodemus.reader.events.UserUnwatchedItem
import com.richodemus.reader.events.UserWatchedItem
import io.reactivex.rxkotlin.subscribeBy
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionService @Inject internal constructor(private val fileSystemPersistence: FileSystemPersistence, val eventStore: EventStore) {
    private val logger = LoggerFactory.getLogger(javaClass)

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
                },
                onError = { logger.error("Subscription service event stream failure", it) },
                onComplete = { logger.info("Subscription service event stream closed") }
        )


        val user = fileSystemPersistence.get(Username("RichoDemus"))!!
        user.let { user ->
            user.feeds.forEach { feed ->
                logger.info("User ${user.id} subbing to ${feed.key}")
                subscribe(user.id, feed.key)
                Thread.sleep(1000L)
                feed.value.forEach { item ->
                    logger.info("Marking item $item in feed ${feed.key} as read")
                    markAsRead(user.id, feed.key, item)
                    Thread.sleep(10L)
                }
                logger.info("Done with feed {}", feed.key)
            }
        }
        logger.info("Done converting data to events")

        Thread.sleep(10000L)

        logger.info("Comparing old and new data")
        val newUser = users[user.id]!!
        val missingFeeds = user.feeds.keys.minus(newUser.feeds.keys)
        if (missingFeeds.isNotEmpty()) {
            logger.warn("Missing feeds {}", missingFeeds)
            throw IllegalStateException("Mssing feeds: " + missingFeeds)
        }

        user.feeds.forEach { feedId, watchedItems ->
            val missingWatchedItems = watchedItems.minus(newUser.feeds[feedId]!!)
            if (missingWatchedItems.isNotEmpty()) {
                logger.warn("Missing items in feed {}: {}", feedId, missingWatchedItems)
                throw IllegalStateException("Missing items in feed $feedId: $missingWatchedItems")
            }
        }

        logger.info("Done comparing, all is ok")
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
