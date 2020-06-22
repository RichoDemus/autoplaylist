package com.richo.reader.youtube_feed_service

import org.slf4j.LoggerFactory
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class Cache<T>
@Inject constructor(
        private val fileSystemPersistence: JsonFileSystemPersistence<T>
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val cache = mutableMapOf<String, T>()

    fun keys() = cache.keys.toList()
    fun values() = cache.values.toList()

    operator fun get(id: String): T? {
        logger.debug("Fetching channel {}", id)

        return cache.computeIfAbsent(id) {
            fileSystemPersistence.getChannel(id).orElse(null)
        }
    }

    operator fun set(id: String, t: T) {
        cache[id] = t
        fileSystemPersistence.updateChannel(id, t)
    }
}
