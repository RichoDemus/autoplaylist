package com.richodemus.reader.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class UsernameTest {
    @Test
    fun `should serialize into just its value when used as a field in another object`() {
        val mapper = ObjectMapper()
        val result = "my-iD"
                .let { Username(it) }
                .let { UserHolder(it) }
                .let { mapper.writeValueAsString(it) }

        //language=JSON
        assertThat(result).isEqualTo("{\"id\":\"my-id\"}")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw exception if feed id is empty`() {
        Username("")
    }

    @Test
    fun `toString should just return id`() {
        assertThat(Username("hello").toString()).isEqualTo("hello")
    }

    @Test
    fun `should be deserializable when used as a value`() {
        val mapper = ObjectMapper()
        mapper.registerModule(KotlinModule())

        //language=JSON
        val result = mapper.readValue("{\"id\":\"Hello\"}", UserHolder::class.java)

        assertThat(result).isEqualTo(UserHolder(Username("hello")))
    }

    @Test
    fun `should serialize into just a string`() {
        val mapper = ObjectMapper()
        val result = mapper.writeValueAsString(Username("Hello"))

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
        val result = mapper.readValue(json, Username::class.java)

        assertThat(result).isEqualTo(Username("hello"))
    }

    @Test
    fun shouldAlwaysConvertToLowercase() {
        assertThat(Username("hello").value).isEqualTo("hello")
        assertThat(Username("Hello").value).isEqualTo("hello")
        assertThat(Username("HELLO").value).isEqualTo("hello")
    }

    @Test
    fun shouldIgnoreCase() {
        assertThat(Username("hello")).isEqualTo(Username("hello"))
        assertThat(Username("Hello")).isEqualTo(Username("hello"))
        assertThat(Username("HELLO")).isEqualTo(Username("hello"))
    }
}

data class UserHolder(val id: Username)
