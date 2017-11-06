package com.richodemus.reader.user_service

import com.richodemus.reader.common.kafka_adapter.EventStore
import com.richodemus.reader.dto.EventId
import com.richodemus.reader.dto.Password
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import com.richodemus.reader.events.ChangePassword
import com.richodemus.reader.events.CreateUser
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserService @Inject internal constructor(private val eventStore: EventStore) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var users = mapOf<Username, User>()

    init {
        eventStore.consume { event ->
            if (event is CreateUser) {
                users = users.plus(Pair(event.username, User(event.userId, event.username, event.password)))
            }
            users = users.mapValues { user -> user.value.process(event) }
        }
    }

    fun create(username: Username, password: Password): UserId {
        assertUserDoesntExist(username) { "User $username already exists" }
        val eventId = EventId(UUID.randomUUID())
        val userId = UserId(UUID.randomUUID().toString())
        logger.info("Creating new user {} ({})", username, userId)

        eventStore.produce(CreateUser(eventId, userId, username, password.hash()))
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
        eventStore.produce(ChangePassword(eventId, userId, password.hash()))
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
