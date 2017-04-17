package com.richo.reader.subscription_service

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.richodemus.reader.dto.Password
import com.richodemus.reader.dto.Username
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject
import javax.inject.Named

internal class FileSystemPersistence @Inject constructor(@Named("saveRoot") val saveRoot: String) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val mapper = ObjectMapper()

    init {
        mapper.registerModule(KotlinModule())
    }

    fun get(id: Username): User? {
        try {
            val file = File("$saveRoot/users/$id/data.json")
            if (!file.exists()) {
                logger.warn("User $id doesn't exist")
                return null
            }
            val user = mapper.readValue(file, User::class.java)
            return user
        } catch(e: Exception) {
            logger.warn("Unable to load user: {}", id, e)
            return null
        }
    }

    fun update(user: User) {
        try {
            val path = saveRoot + "/users/" + user.name
            val success = File(path).mkdirs()
            logger.trace("Creating {} successful: {}", path, success)
            val pp = DefaultPrettyPrinter()
            pp.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE)
            mapper.writer(pp).writeValue(File(path + "/data.json"), user)
        } catch(e: Exception) {
            logger.error("Unable to create/update user {}", user.name, e)
            throw e
        }
    }

    fun setPassword(username: Username, password: Password) {
        try {
            val path = saveRoot + "/users/" + username
            val success = File(path).mkdirs()
            logger.trace("Creating {} successful: {}", path, success)
            val pp = DefaultPrettyPrinter()
            pp.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE)
            mapper.writer(pp).writeValue(File(path + "/password.json"), password)
        } catch(e: Exception) {
            logger.error("Unable to set password for user {}", username, e)
            throw e
        }
    }

    fun getPassword(username: Username): Password? {
        try {
            val file = File("$saveRoot/users/$username/password.json")
            val user = mapper.readValue(file, Password::class.java)
            return user
        } catch(e: Exception) {
            logger.warn("Unable to load password for user: {}", username, e)
            return null
        }
    }
}
