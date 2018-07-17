package com.richodemus.autoplaylist.user

import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.event.EventStore
import com.richodemus.autoplaylist.event.UserCreated
import com.richodemus.autoplaylist.spotify.SpotifyPort
import io.github.vjames19.futures.jdk8.Future
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named
class UserService @Inject internal constructor(
        private val eventStore: EventStore,
        private val spotifyPort: SpotifyPort
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val users = mutableListOf<User>()
    private val futures = mutableMapOf<SpotifyUserId, MutableList<CompletableFuture<User>>>()

    init {
        eventStore.consume {
            when (it) {
                is UserCreated -> add(it)
                else -> logger.debug("Event of type: ${it.type()} not handled")
            }
        }
    }

    // todo better synchronization
    @Synchronized
    fun findOrCreateUser(spotifyUserId: SpotifyUserId): CompletableFuture<User> {
        val matchingUsers = users.filter { it.spotifyUserId == spotifyUserId }
        if (matchingUsers.size > 1) {
            logger.warn("There seems to be more than 1 user stored with the id {}", spotifyUserId)
            return Future { matchingUsers[0] }
        }

        if (matchingUsers.size == 1) {
            logger.info("User for spotify id {} already exists", spotifyUserId)
            return Future { matchingUsers[0] }
        }

        // User does not exist, need to create it
        logger.info("Creating user for spotify id {}", spotifyUserId)
        val future = CompletableFuture<User>()
        futures.computeIfAbsent(spotifyUserId) { mutableListOf() }.add(future)
        eventStore.produce(UserCreated(spotifyUserId = spotifyUserId))
        return future
    }

    fun getUser(userId: UserId): User? {
        val matchingUsers = users.filter { it.userId == userId }
        if (matchingUsers.size > 1) {
            logger.warn("{} users with the id {}", matchingUsers.size, userId)
        }
        return matchingUsers.firstOrNull()
    }

    @Synchronized
    private fun add(userCreated: UserCreated) {
        logger.info("Creating user: {}", userCreated)
        val user = User(spotifyPort, userCreated.userId, userCreated.spotifyUserId)
        users.add(user)
        futures[user.spotifyUserId]?.forEach { it.complete(user) }
        futures.remove(user.spotifyUserId)
    }
}
