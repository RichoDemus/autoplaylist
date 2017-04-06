package com.richo.reader.user_service

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.richodemus.reader.dto.Password
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import java.util.UUID
import java.util.concurrent.TimeUnit.DAYS
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserService @Inject internal constructor(private val fileSystemPersistence: FileSystemPersistence) {
    private val CACHE_SIZE = 10000L

    val cache: LoadingCache<Username, User?> = Caffeine.newBuilder()
            .maximumSize(CACHE_SIZE)
            .expireAfterAccess(1, DAYS)
            .build { id: Username -> fileSystemPersistence.get(id) }

    fun create(username: Username, password: Password): UserId {
        assertUserDoesntExist(username) { "User $username already exists" }
        val id = UserId(UUID.randomUUID().toString())
        fileSystemPersistence.update(User(id, username, emptyMap(), 0L, listOf()))
        updatePassword(username, password)
        return id
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

    fun isPasswordValid(username: Username, password: Password): Boolean = password == fileSystemPersistence.getPassword(username)

    fun updatePassword(username: Username, password: Password) {
        assertUserExists(username) { "Can't change password for user $username because it doesn't exist" }
        fileSystemPersistence.setPassword(username, password)
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
