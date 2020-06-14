package com.richodemus.reader.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SessionTest {
    @Test
    fun `Test Serialization`() {
        val mapper = ObjectMapper()

        val result = mapper.writeValueAsString(Session(Username("richo"), "token"))

        //language=JSON
        val expected = "{\"username\":\"richo\",\"token\":\"token\"}"
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `Test Deserialization`() {
        val mapper = ObjectMapper()
        mapper.registerModule(KotlinModule())

        //language=JSON
        val json = "{\"username\":\"richo\",\"token\":\"token\"}"

        val result = mapper.readValue(json, Session::class.java)

        assertThat(result).isEqualToComparingFieldByField(Session(Username("richo"), "token"))
    }
}
