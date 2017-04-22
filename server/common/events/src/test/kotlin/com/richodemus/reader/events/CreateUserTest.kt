package com.richodemus.reader.events

import com.fasterxml.jackson.databind.ObjectMapper
import com.richodemus.reader.dto.EventId
import com.richodemus.reader.dto.PasswordHash
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.UUID

class CreateUserTest {
    @Test
    fun `should serialize into something sensible`() {
        val mapper = ObjectMapper()
        val result = mapper.writeValueAsString(CreateUser(EventId(UUID.fromString("f4d911c8-0783-46e8-82a4-371ef25ae7ca")), UserId("cool-id"), Username("richo"), PasswordHash("my_password")))

        //language=JSON
        val expected = "{\"eventId\":\"f4d911c8-0783-46e8-82a4-371ef25ae7ca\",\"type\":\"CREATE_USER\",\"id\":\"cool-id\",\"username\":\"richo\",\"password\":\"my_password\"}"
        assertThat(result).isEqualTo(expected)
    }
}
