package com.richodemus.reader.youtube_feed_service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
internal class Cache<K,V>(
        private val fileSystemPersistence: JsonFileSystemPersistence<K,V>
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val cache = mutableMapOf<K, V>()

    fun keys() = cache.keys.toList()
    fun values() = cache.values.toList()

    operator fun get(id: K): V? {
        logger.debug("Fetching channel {}", id)

        return cache.computeIfAbsent(id) {
            fileSystemPersistence.getChannel(id).orElse(null)
        }
    }

    operator fun set(id: K, t: V) {
        cache[id] = t
        fileSystemPersistence.updateChannel(id, t)
    }
}
