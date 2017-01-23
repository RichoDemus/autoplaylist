package com.richo.reader.user_service

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.richodemus.reader.dto.Password
import com.richodemus.reader.dto.Username
import java.util.concurrent.TimeUnit.DAYS
import javax.inject.Inject

class UserService @Inject internal constructor(private val fileSystemPersistence: FileSystemPersistence) {
    private val CACHE_SIZE = 10000L

    val cache: LoadingCache<Username, User?> = Caffeine.newBuilder()
            .maximumSize(CACHE_SIZE)
            .expireAfterAccess(1, DAYS)
            .build { id: Username -> fileSystemPersistence.get(id) }


    fun get(id: Username): User? {
        return cache.get(id)
    }

    fun update(user: User) {
        fileSystemPersistence.update(user)
        cache.invalidate(user.name)
    }

    fun isPasswordValid(username: Username, password: Password): Boolean = password == fileSystemPersistence.getPassword(username)

    fun updatePassword(username: Username, password: Password) {
        fileSystemPersistence.setPassword(username, password)
    }
}
