package com.richo.reader.youtube_feed_service

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.*
import org.slf4j.LoggerFactory
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Optional
import javax.inject.Inject
import javax.inject.Named

class JsonFileSystemPersistence<T>
@Inject internal constructor(
        @Named("saveRoot") private val saveRoot: String,
        @Named("fileName") private val fileName: String,
        private val clazz: Class<T>
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val objectMapper = jacksonObjectMapper()
            .apply { this.findAndRegisterModules() }
            .apply { this.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false) }

    fun getChannel(id: String): Optional<T> {
        return try {
            val file = File("$saveRoot/feeds/$id/$fileName.json")
            if (!file.exists()) {
                logger.debug("Feed {} not on disk", id)
                return Optional.empty<T>()
            }
            logger.trace("Reading feed {} from disk", id)
            Optional.ofNullable(objectMapper.readValue(file,clazz))
        } catch (e: Exception) {
            logger.warn("Unable to load feed: {}", id, e)
            Optional.empty<T>()
        }
    }

    fun updateChannel(id: String, item: T) {
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
        }
    }

}
