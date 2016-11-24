package com.richodemus.reader.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FeedIdTest {
    @Test
    fun `should serialize into just its value when used as a field in another object`() {
        val mapper = ObjectMapper()
        mapper.registerModule(KotlinModule())
        val result = "my-id"
                .let { FeedId(it) }
                .let { Channel(it) }
                .let { mapper.writeValueAsString(it) }

        //language=JSON
        assertThat(result).isEqualTo("{\"id\":\"my-id\"}")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw exception if feed id is empty`() {
        FeedId("")
    }

    @Test
    fun `toString should just return id`() {
        assertThat(FeedId("hello").toString()).isEqualTo("hello")
    }
}

data class Channel(val id: FeedId)
