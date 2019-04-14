package com.richodemus.autoplaylist.usermapping

import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.dto.events.UserIdsMapped
import com.richodemus.autoplaylist.event.EventStore
import org.slf4j.LoggerFactory
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named
class UserIdMappingService(private val eventStore: EventStore) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var userMappings = emptyList<Pair<UserId, SpotifyUserId>>()

    init {
        eventStore.consume { event ->
            if (event is UserIdsMapped) {
                logger.info("Mapping ${event.spotifyUserId} to ${event.userId}")
                createMapping(event.userId, event.spotifyUserId)
            }
        }
    }

    private fun createMapping(userId: UserId, spotifyUserId: SpotifyUserId) {
        if(userMappings.any { it.first == userId }) {
            logger.warn("There is already a user with id $userId")
            return
        }
        if(userMappings.any { it.second == spotifyUserId }) {
            logger.warn("There is already a user with spotify user id $userId")
            return
        }
        userMappings = userMappings + Pair(userId, spotifyUserId)
    }

    fun getUserId(spotifyUserId: SpotifyUserId): UserId {
        val matchingMappings = userMappings
                .filter { (_, id) -> id == spotifyUserId }
        if(matchingMappings.size > 1) {
            logger.warn("There are multiple mappings for spotify user id $spotifyUserId: $matchingMappings")
        }
        if(matchingMappings.isNotEmpty()) {
            return matchingMappings.first().first
        }

        // new spotify user id
        val event = UserIdsMapped.create(spotifyUserId)
        eventStore.produce(event)
        return event.userId
    }

    fun getUserId(userId: UserId) : SpotifyUserId? {
        return userMappings.find { it.first == userId }?.second
    }
}
