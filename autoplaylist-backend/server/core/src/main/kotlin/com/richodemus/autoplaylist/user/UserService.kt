package com.richodemus.autoplaylist.user

import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.dto.events.Event
import com.richodemus.autoplaylist.dto.events.PlaylistCreated
import com.richodemus.autoplaylist.dto.events.UserCreated
import com.richodemus.autoplaylist.event.EventStore
import com.richodemus.autoplaylist.spotify.SpotifyPort
import org.slf4j.LoggerFactory
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named
class UserService(
        private val eventStore: EventStore,
        private val spotifyPort: SpotifyPort
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    var users = mapOf<UserId, User>()
        private set

    init {
        eventStore.consume {
            when (it) {
                is UserCreated -> add(it)
                is PlaylistCreated -> process(it.userId, it)
                else -> logger.debug("Event of type: ${it.type()} not handled")
            }
        }
    }

    // todo better synchronization
    @Synchronized
    fun findOrCreateUser(spotifyUserId: SpotifyUserId, refreshToken: RefreshToken): User {
        val matchingUsers = users.values.filter { it.spotifyUserId == spotifyUserId }
        if (matchingUsers.isNotEmpty()) {
            if (matchingUsers.size > 1) {
                logger.warn("There seems to be more than 1 user stored with the id {}", spotifyUserId)
            }
            if (matchingUsers.size == 1) {
                logger.info("User for spotify id {} already exists", spotifyUserId)
                val user = matchingUsers[0]
                user.refreshToken = refreshToken
                return user
            }
        }

        // User does not exist, need to create it
        logger.info("Creating user for spotify id {}", spotifyUserId)
        val event = UserCreated(spotifyUserId = spotifyUserId, refreshToken = refreshToken)
        val user = add(event)
        eventStore.produce(event)
        return user
    }

    fun getUser(userId: UserId) = users[userId]

    @Synchronized
    private fun add(userCreated: UserCreated): User {
        logger.info("Creating user: {}", userCreated)
        val user = User(eventStore, spotifyPort, userCreated.userId, userCreated.spotifyUserId, userCreated.refreshToken)
        if (users.contains(user.userId)) {
            logger.warn("There is already a user with this id, existing: {}, new: {}", users[user.userId], user)
        } else {
            users = users.plus(user.userId to user)
        }
        return user
    }

    private fun process(userId: UserId, event: Event) {
        val user = users[userId]
        // todo create user if it doesnt exist and then fill it if UserCreated comes
        if (user == null) {
            logger.warn("No such user, can't process event $event")
            return
        }

        user.process(event)
    }
}
