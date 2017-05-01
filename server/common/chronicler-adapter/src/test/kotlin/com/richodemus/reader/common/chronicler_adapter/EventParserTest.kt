package com.richodemus.reader.common.chronicler_adapter

import com.richodemus.reader.events.CreateUser
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class EventParserTest {
    @Test
    fun `Parse CreateUser Event`() {
        //language=JSON
        val eventString = "{\"eventId\":\"39bf0d95-ee23-422b-832e-efa4312fb094\",\"userId\":\"77fefb7a-bc57-4ad1-aced-31d6d2c0c830\",\"username\":\"richodemus\",\"password\":\"$2a$10qSJfNgcZ2ZEu/lxI/XIQVuK2kOOHbrmGJT5FBmYh.o/1HRfU16.DC\",\"type\":\"CREATE_USER\"}"

        val result = eventString.toEvent() as CreateUser

        assertThat(result.userId.value).isEqualTo("77fefb7a-bc57-4ad1-aced-31d6d2c0c830")
    }
}
