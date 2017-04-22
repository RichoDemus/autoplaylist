package com.richo.reader.subscription_service

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
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
        fileSystemPersistence.update(User(id, username, emptyMap(), 0L, listOf()))
    }

    fun find(id: Username): User? {
        return cache.get(id)
    }

    fun exists(id: Username) = cache.get(id) != null

    fun update(user: User) {
        assertUserExists(user.name) { "Can't update user ${user.name} because it doesn't exist" }
        fileSystemPersistence.update(user)
        cache.invalidate(user.name)
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
}
