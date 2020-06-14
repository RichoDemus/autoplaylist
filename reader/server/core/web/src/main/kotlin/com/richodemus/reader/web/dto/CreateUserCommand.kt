package com.richodemus.reader.web.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.richodemus.reader.dto.Password
import com.richodemus.reader.dto.Username

data class CreateUserCommand @JsonCreator
constructor(@param:JsonProperty("username") val username: Username,
            @param:JsonProperty("password") val password: Password,
            @param:JsonProperty("inviteCode") val inviteCode: String)
