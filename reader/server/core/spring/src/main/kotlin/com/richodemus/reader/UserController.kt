package com.richodemus.reader

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.richodemus.reader.dto.Password
import com.richodemus.reader.dto.Username
import com.richodemus.reader.user_service.UserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(
        origins = ["http://localhost:8080", "https://reader.richodemus.com"],
        maxAge = 3600,
        allowCredentials = "true"
)
@RestController
internal class UserController(
        @Value("\${inviteCode}") private val inviteCode: String, //todo fix code
        private val userService: UserService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Suppress("unused")
    @PostMapping("v1/users")
    internal fun createUser(@RequestBody createUserCommand: CreateUserCommand): ResponseEntity<String> {
        if (inviteCode != createUserCommand.inviteCode) {
            logger.info("{} tried to signup with invalid code {}", createUserCommand.username, createUserCommand.inviteCode)
            return ResponseEntity(FORBIDDEN)
        }
        try {
            userService.create(createUserCommand.username, createUserCommand.password)
        } catch (e: Exception) {
            logger.error("Exception when creating user {}", e)
            return ResponseEntity(INTERNAL_SERVER_ERROR)
        }
        return ResponseEntity.ok("${createUserCommand.username} created")
    }

    data class CreateUserCommand @JsonCreator
    constructor(@param:JsonProperty("username") val username: Username,
                @param:JsonProperty("password") val password: Password,
                @param:JsonProperty("inviteCode") val inviteCode: String)
}
