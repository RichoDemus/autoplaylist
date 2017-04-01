package com.richodemus.reader.web.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.richodemus.reader.dto.UserId

data class CreateUserCommand @JsonCreator
constructor(@param:JsonProperty("username") val username: UserId,
            @param:JsonProperty("password") val password: String,
            @param:JsonProperty("inviteCode") val inviteCode: String)
