package com.richodemus.reader.common.chronicler_adapter

import com.richodemus.reader.dto.EventId
import com.richodemus.reader.dto.PasswordHash
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import com.richodemus.reader.events.CreateUser
import com.richodemus.reader.events.Event
import io.reactivex.rxkotlin.subscribeBy
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.Ignore
import org.junit.Test
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

class ChroniclerAdapterTest {
    @Ignore("Uses a live Chronicler instance, hard to mock SSE")
    @Test
    fun `Should emit added event`() {
        val target = ChroniclerAdapter()
        val receivedEvents = mutableListOf<Event>()

        target.observe().subscribeBy(onNext = { receivedEvents.add(it) })

        val id = EventId(UUID.randomUUID())
        target.add(CreateUser(id, UserId("user-id"), Username("username"), PasswordHash("hash")))

        await().atMost(10, TimeUnit.SECONDS).until(Callable { receivedEvents.isNotEmpty() })

        assertThat(receivedEvents).isNotEmpty
        assertThat(receivedEvents.map { it.eventId }).contains(id)

    }
}
