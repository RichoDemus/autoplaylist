package com.richodemus.reader.youtube_feed_service

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.lang.RuntimeException
import java.util.Optional

@Component
class JsonFileSystemPersistence<K, V>(
        private val saveRoot: String,
        private val fileName: String,
        private val clazz: Class<V>?
) {

    // todo this is a weird hack to please spring....
    @Autowired
    constructor() : this("", "", null) {

    }

    private val logger = LoggerFactory.getLogger(javaClass)
    private val objectMapper = jacksonObjectMapper()
            .apply { this.findAndRegisterModules() }
            .apply { this.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false) }

    fun getChannel(id: K): Optional<V> {
        return try {
            val file = File("$saveRoot/feeds/$id/$fileName.json")
            if (!file.exists()) {
                logger.debug("Feed {} not on disk", id)
                return Optional.empty<V>()
            }
            logger.trace("Reading feed {} from disk", id)
            Optional.ofNullable(objectMapper.readValue(file, clazz))
        } catch (e: Exception) {
            logger.warn("Unable to load feed: {}", id, e)
            Optional.empty<V>()
        }
    }

    fun updateChannel(id: K, item: V) {
        try {
            val path = saveRoot + "/feeds/" + id
            val success = File(path).mkdirs()
            logger.trace("Creating {} successful: {}", path, success)
            val file = File("$path/$fileName.json")
            objectMapper.writeValue(file, item)
            val asString = objectMapper.writeValueAsString(item)
            BufferedWriter(FileWriter(file)).use { writer -> writer.write(asString) }
        } catch (e: IOException) {
            logger.warn("Unable to write feed {} to disk", id, e)
            throw RuntimeException("Unable to write feed $id to disk", e);
        }
    }

}
