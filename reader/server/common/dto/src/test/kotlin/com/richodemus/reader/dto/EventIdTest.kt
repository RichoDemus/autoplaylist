package com.richodemus.reader.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.assertj.core.api.Assertions
import org.junit.Test
import java.util.UUID

class EventIdTest {
    @Test
    fun `should serialize into just a string`() {
        val mapper = ObjectMapper()
        val result = mapper.writeValueAsString("be4efe3a-157e-40be-8302-3f2b4eaa8eea".toEventId())

        //language=JSON
        val expected = "\"be4efe3a-157e-40be-8302-3f2b4eaa8eea\""
        Assertions.assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `should deserialize from just a string`() {
        val mapper = ObjectMapper()
        mapper.registerModule(KotlinModule())

        //language=JSON
        val json = "\"be4efe3a-157e-40be-8302-3f2b4eaa8eea\""
        val result = mapper.readValue(json, EventId::class.java)

        val expected = "be4efe3a-157e-40be-8302-3f2b4eaa8eea".toEventId()
        Assertions.assertThat(result).isEqualTo(expected)
    }

    private fun String.toEventId() = EventId(UUID.fromString(this))
}