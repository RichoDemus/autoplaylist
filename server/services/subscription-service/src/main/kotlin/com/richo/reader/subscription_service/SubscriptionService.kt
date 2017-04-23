package com.richo.reader.subscription_service

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import com.richodemus.reader.events.CreateUser
import io.reactivex.rxkotlin.subscribeBy
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit.DAYS
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionService @Inject internal constructor(private val fileSystemPersistence: FileSystemPersistence, val eventStore: EventStore) {
    private val CACHE_SIZE = 10000L
    private val logger = LoggerFactory.getLogger(javaClass)

    val users = mutableMapOf<UserId, User>()

    val cache: LoadingCache<Username, User?> = Caffeine.newBuilder()
            .maximumSize(CACHE_SIZE)
            .expireAfterAccess(1, DAYS)
            .build { id: Username -> fileSystemPersistence.get(id) }

    init {
        eventStore.observe().subscribeBy(
                onNext = {
                    if (it is CreateUser) {
                        create(it.id, it.username)
                    } else {
                        logger.warn("Event of type: ${it.javaClass} not handled")
                    }
                },
                onError = { logger.error("Subscription service event stream failure", it) },
                onComplete = { logger.info("Subscription service event stream closed") }
        )
    }

    private fun create(id: UserId, username: Username) {
        assertUserDoesntExist(username) { "User $username already exists" }
        val user = User(id, username, mutableMapOf(), 0L, listOf())
        fileSystemPersistence.update(user)
        users.put(user.id, user)
    }

    fun find(id: Username): User? {
        return cache.get(id)
    }

    fun exists(id: Username) = cache.get(id) != null
    fun exists(id: UserId) = users.containsKey(id)

    fun update(user: User) {
        assertUserExists(user.name) { "Can't update user ${user.name} because it doesn't exist" }
        fileSystemPersistence.update(user)
        users.put(user.id, user)
        cache.invalidate(user.name)
    }

    fun subscribe(userId: UserId, feedId: FeedId) {
        assertUserExists(userId) { "User $userId can't subscribe to feed $feedId: User does not exist"}
        val user = users[userId]!!
        user.subscribe(feedId)

        update(user)
    }

    fun markAsRead(userId: UserId, feedId: FeedId, itemId: ItemId) {
        assertUserExists(userId) { "User $userId can't mark item $itemId as read in feed $feedId: User does not exist"}
        val user = users[userId]!!
        user.watch(feedId, itemId)

        update(user)
    }

    fun  markAsUnread(userId: UserId, feedId: FeedId, itemId: ItemId) {
        assertUserExists(userId) { "User $userId can't mark item $itemId as read in feed $feedId: User does not exist"}
        val user = users[userId]!!
        user.unWatch(feedId, itemId)

        update(user)
    }

    private fun assertUserExists(username: Username, msg: () -> String) {
        if (!exists(username)) {
            throw IllegalStateException(msg.invoke())
        }
    }

    private fun assertUserDoesntExist(username: Username, msg: () -> String) {
        if (exists(username)) {
            throw IllegalStateException(msg.invoke())
        }
    }

    private fun assertUserExists(userId: UserId, msg: () -> String) {
        if (!exists(userId)) {
            throw IllegalStateException(msg.invoke())
        }
    }
}
