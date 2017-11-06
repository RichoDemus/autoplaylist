package com.richodemus.reader.events

import com.fasterxml.jackson.databind.ObjectMapper
import com.richodemus.reader.dto.PasswordHash
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CreateUserTest {
    @Test
    fun `should serialize into something sensible`() {
        val mapper = ObjectMapper()
        val result = mapper.writeValueAsString(CreateUser(UserId("cool-id"), Username("richo"), PasswordHash("my_password")))

        //language=REGEXP
        val expected3 = ".\"eventId\":\"[a-z0-9\\-]*\",\"type\":\"CREATE_USER\",\"userId\":\"cool-id\",\"username\":\"richo\",\"password\":\"my_password\"."
        assertThat(result).matches(expected3)
    }
}
