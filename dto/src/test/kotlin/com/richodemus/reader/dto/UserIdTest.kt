package com.richodemus.reader.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class UserIdTest {
    @Test
    fun `should serialize into just its value when used as a field in another object`() {
        val mapper = ObjectMapper()
        val result = "my-iD"
                .let { UserId(it) }
                .let { UserHolder(it) }
                .let { mapper.writeValueAsString(it) }

        //language=JSON
        assertThat(result).isEqualTo("{\"id\":\"my-id\"}")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw exception if feed id is empty`() {
        UserId("")
    }

    @Test
    fun `toString should just return id`() {
        assertThat(UserId("hello").toString()).isEqualTo("hello")
    }

    @Test
    fun `should be deserializable when used as a value`() {
        val mapper = ObjectMapper()
        mapper.registerModule(KotlinModule())

        //language=JSON
        val result = mapper.readValue("{\"id\":\"Hello\"}", UserHolder::class.java)

        assertThat(result).isEqualTo(UserHolder(UserId("hello")))
    }

    @Test
    fun `should serialize into just a string`() {
        val mapper = ObjectMapper()
        val result = mapper.writeValueAsString(UserId("Hello"))

        //language=JSON
        val expected = "\"hello\""
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `should deserialize from just a string`() {
        val mapper = ObjectMapper()
        mapper.registerModule(KotlinModule())

        //language=JSON
        val json = "\"Hello\""
        val result = mapper.readValue(json, UserId::class.java)

        assertThat(result).isEqualTo(UserId("hello"))
    }

    @Test
    fun shouldAlwaysConvertToLowercase() {
        assertThat(UserId("hello").value).isEqualTo("hello")
        assertThat(UserId("Hello").value).isEqualTo("hello")
        assertThat(UserId("HELLO").value).isEqualTo("hello")
    }

    @Test
    fun shouldIgnoreCase() {
        assertThat(UserId("hello")).isEqualTo(UserId("hello"))
        assertThat(UserId("Hello")).isEqualTo(UserId("hello"))
        assertThat(UserId("HELLO")).isEqualTo(UserId("hello"))
    }
}

data class UserHolder(val id: UserId)