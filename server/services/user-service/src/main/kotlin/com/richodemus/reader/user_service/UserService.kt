package com.richodemus.reader.user_service

import com.richodemus.reader.dto.EventId
import com.richodemus.reader.dto.Password
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import com.richodemus.reader.events.ChangePassword
import com.richodemus.reader.events.CreateUser
import io.reactivex.rxkotlin.subscribeBy
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserService @Inject internal constructor(val eventStore: EventStore) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val users = mutableMapOf<Username, User>()

    init {
        eventStore.observe().subscribeBy(
                onNext = {
                    if (it is CreateUser) {
                        logger.info("Adding user {} ({})", it.username, it.userId)
                        users.put(it.username, User(it.userId, it.username, it.password))
                    } else if (it is ChangePassword) {
                        val user = users.values.singleOrNull { user -> user.id == it.userId }
                        if (user == null) {
                            logger.warn("Got Change password event for non existing user ${it.userId}")
                        } else {
                            user.password = it.password
                        }
                    } else {
                        logger.warn("Event of type: ${it.javaClass} not handled")
                    }
                },
                onError = { logger.error("User service event stream failure", it) },
                onComplete = { logger.info("User service event stream closed") }
        )
    }

    fun create(username: Username, password: Password): UserId {
        assertUserDoesntExist(username) { "User $username already exists" }
        val eventId = EventId(UUID.randomUUID())
        val userId = UserId(UUID.randomUUID().toString())
        logger.info("Creating new user {} ({})", username, userId)

        eventStore.add(CreateUser(eventId, userId, username, password.hash()))

        return userId
    }

    fun find(username: Username): User? {
        return users[username]
    }

    fun passwordValid(username: Username, password: Password): Boolean {
        return users[username]?.password?.isSame(password) ?: false
    }

    fun changePassword(userId: UserId, password: Password) {
        assertUserExists(userId) { "No user with id $userId" }

        val eventId = EventId(UUID.randomUUID())
        eventStore.add(ChangePassword(eventId, userId, password.hash()))
    }

    private fun assertUserExists(userId: UserId, msg: () -> String) {
        if (users.values.map(User::id).contains(userId).not()) {
            throw IllegalStateException(msg.invoke())
        }
    }

    private fun assertUserDoesntExist(username: Username, msg: () -> String) {
        if (exists(username)) {
            throw IllegalStateException(msg.invoke())
        }
    }

    private fun exists(id: Username) = users[id] != null
}
