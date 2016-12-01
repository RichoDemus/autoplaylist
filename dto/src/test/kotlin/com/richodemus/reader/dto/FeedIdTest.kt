package com.richodemus.reader.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test

class FeedIdTest {
    @Test
    fun `should serialize into just its value when used as a field in another object`() {
        val mapper = ObjectMapper()
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

    @Test
    fun `should be deserializable when used as a value`() {
        val mapper = ObjectMapper()
        mapper.registerModule(KotlinModule())

        //language=JSON
        val result = mapper.readValue("{\"id\":\"hello\"}", Channel::class.java)

        assertThat(result).isEqualTo(Channel(FeedId("hello")))
    }

    @Ignore("Can't get this to work")
    @Test
    fun `should serialize into just it's value`() {
        val mapper = ObjectMapper()
        val result = mapper.writeValueAsString(FeedId("hello"))

        assertThat(result).isEqualTo("hello")
    }

    @Ignore("Can't get this to work")
    @Test
    fun `should deserialize from just a string`() {
        val mapper = ObjectMapper()
        mapper.registerModule(KotlinModule())

        //language=JSON
        val result = mapper.readValue("hello", FeedId::class.java)

        assertThat(result).isEqualTo(FeedId("hello"))
    }
}

data class Channel(val id: FeedId)
