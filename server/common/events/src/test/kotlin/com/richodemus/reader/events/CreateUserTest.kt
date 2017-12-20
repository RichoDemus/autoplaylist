package com.richodemus.reader.events

import com.fasterxml.jackson.databind.ObjectMapper
import com.richodemus.reader.dto.EventId
import com.richodemus.reader.dto.PasswordHash
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import com.richodemus.reader.events_v2.EventType
import com.richodemus.reader.events_v2.UserCreated
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.UUID

class CreateUserTest {
    @Test
    fun `should serialize into something sensible`() {
        val mapper = ObjectMapper()
        val result = mapper.writeValueAsString(UserCreated(EventId(UUID.fromString("78157356-dac1-4af1-8997-9b9e8d10f671")), "timestamp", EventType.USER_CREATED, UserId("user-id"), Username("richo"), PasswordHash("my_password")))

        assertThat(result).isEqualTo("{\"id\":\"78157356-dac1-4af1-8997-9b9e8d10f671\",\"timestamp\":\"timestamp\",\"type\":\"USER_CREATED\",\"userId\":\"user-id\",\"username\":\"richo\",\"password\":\"my_password\"}")
    }
}
