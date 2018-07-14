package com.richodemus.autoplaylist.user

import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.UserId
import io.github.vjames19.futures.jdk8.Future
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import javax.inject.Singleton

@Singleton
@Service
internal class UserService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val users = mutableListOf<User>()

    // todo better synchronization
    @Synchronized
    fun findOrCreateUser(spotifyUserId: SpotifyUserId): CompletableFuture<User> {
        return Future {
            var matchingUsers = users.filter { it.spotifyUserId == spotifyUserId }
            if (matchingUsers.size > 1) {
                logger.warn("There seems to be more than 1 user stored with the id {}", spotifyUserId)
                matchingUsers[0]
            }
            if (matchingUsers.isEmpty()) {
                matchingUsers = listOf(User(UserId(), spotifyUserId))
                users.addAll(matchingUsers)
            }
            matchingUsers[0]
        }
    }

    fun getUser(userId: UserId): User? {
        val matchingUsers = users.filter { it.userId == userId }
        if (matchingUsers.size > 1) {
            logger.warn("{} users with the id {}", matchingUsers.size, userId)
        }
        return matchingUsers.firstOrNull()
    }
}
