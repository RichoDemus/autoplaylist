package com.richodemus.autoplaylist.event

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.dto.events.EventId
import com.richodemus.autoplaylist.dto.events.EventType.USER_CREATED
import com.richodemus.autoplaylist.dto.events.UserCreated
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.UUID


class EventSerdeTest {
    private val mapper = jacksonObjectMapper()

    @Test
    fun `Serialize event`() {
        val target = EventSerde()

        val result = target.serialize(UserCreated(
                EventId("8f8d3ee2-ac7b-4b5c-bdcf-1f490275b4c2".toUUID()),
                USER_CREATED,
                "asd",
                UserId("3076d07b-a289-4676-a7d2-4c7b7c1ea9de".toUUID())
        ))

        assertJsonEquals(result, """
            {
                "id":"8f8d3ee2-ac7b-4b5c-bdcf-1f490275b4c2",
                "type":"USER_CREATED",
                "timestamp":"asd",
                "userId":"3076d07b-a289-4676-a7d2-4c7b7c1ea9de"
            }
            """.trimIndent())
    }

    @Test
    fun `Deserialize event`() {
        val target = EventSerde()

        val result = target.deserialize("""
            {
                "id":"8f8d3ee2-ac7b-4b5c-bdcf-1f490275b4c2",
                "type":"USER_CREATED",
                "timestamp":"asd",
                "userId":"3076d07b-a289-4676-a7d2-4c7b7c1ea9de"
            }
            """.trimIndent().toByteArray())

        assertThat(result).isEqualTo(UserCreated(
                EventId("8f8d3ee2-ac7b-4b5c-bdcf-1f490275b4c2".toUUID()),
                USER_CREATED,
                "asd",
                UserId("3076d07b-a289-4676-a7d2-4c7b7c1ea9de".toUUID())
        ))
    }

    private fun String.toUUID() = UUID.fromString(this)

    // todo write custom assertJ assert
    private fun assertJsonEquals(actual: ByteArray, expected: String) {
        val actualJson = mapper.readTree(actual)
        val expectedJson = mapper.readTree(expected)

        assertThat(actualJson).isEqualTo(expectedJson)
    }
}
