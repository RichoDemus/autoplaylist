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
    private val users = mutableMapOf<UserId, User>()
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
        val matchingUsers = users.values.filter { it.spotifyUserId == spotifyUserId }
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

    fun getUser(userId: UserId) = users[userId]

    @Synchronized
    private fun add(userCreated: UserCreated) {
        logger.info("Creating user: {}", userCreated)
        val user = User(spotifyPort, userCreated.userId, userCreated.spotifyUserId)
        if (users.contains(user.userId)) {
            logger.warn("There is already a user with this id, existing: {}, new: {}", users[user.userId], user)
        } else {
            users[user.userId] = user
        }
        futures[user.spotifyUserId]?.forEach { it.complete(user) }
        futures.remove(user.spotifyUserId)
    }
}
