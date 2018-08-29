package com.richodemus.autoplaylist.event

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.eventstore.Event
import com.richodemus.autoplaylist.eventstore.EventId
import com.richodemus.autoplaylist.eventstore.EventType.USER_CREATED
import com.richodemus.autoplaylist.eventstore.UserCreated
import io.github.vjames19.futures.jdk8.Future
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.util.UUID
import java.util.concurrent.CompletableFuture


class GoogleCloudEventStoreTest {
    private val eventJson = "{\"id\":\"2b041378-66af-4cb0-a2b0-186fc0bdc139\",\"type\":\"USER_CREATED\",\"timestamp\":\"2018-08-29T11:27:14.623013Z\",\"userId\":\"2b041378-66af-4cb0-a2b0-186fc0bdc139\",\"spotifyUserId\":\"user\",\"refreshToken\":\"refresh-token\"}".trimIndent().toByteArray()

    private val event = UserCreated(
            EventId(UUID.fromString("2b041378-66af-4cb0-a2b0-186fc0bdc139")),
            USER_CREATED,
            "2018-08-29T11:27:14.623013Z",
            UserId(UUID.fromString("2b041378-66af-4cb0-a2b0-186fc0bdc139")),
            SpotifyUserId("user"),
            RefreshToken("refresh-token")
    )

    private lateinit var registryMock: MeterRegistry

    @Before
    fun setUp() {
        registryMock = mock { _ ->
            on { counter("events") } doReturn mock<Counter> {}
        }
    }

    @Test
    fun `Init eventstore with empty storage`() {
        val clientMock = mock<GoogleCloudStorage> {
            on { read() } doReturn Future { emptyList<Pair<Long, CompletableFuture<ByteArray>>>() }
        }

        val target = GoogleCloudEventStore(GoogleCloudStorageAdapter(EventSerde(), clientMock), registryMock)

        val events = mutableListOf<Event>()
        target.consume { event ->
            events.add(event)
        }

        assertThat(events).isEmpty()
    }

    @Test(timeout = 60_000L)
    fun `Init eventstore with events in storage`() {
        val clientMock = mock<GoogleCloudStorage> {
            on { read() } doReturn Future {
                listOf(0L to Future {
                    eventJson
                })
            }
        }

        val target = GoogleCloudEventStore(GoogleCloudStorageAdapter(EventSerde(), clientMock), registryMock)

        val events = mutableListOf<Event>()
        target.consume { event ->
            events.add(event)
        }

        while (events.isEmpty()) {
            Thread.sleep(10L)
        }
        assertThat(events).containsOnly(event)
    }

    @Test
    fun `Produce message`() {
        val clientMock = mock<GoogleCloudStorage> {
            on { read() } doReturn Future { emptyList<Pair<Long, CompletableFuture<ByteArray>>>() }
        }

        val target = GoogleCloudEventStore(GoogleCloudStorageAdapter(EventSerde(), clientMock), registryMock)

        val events = mutableListOf<Event>()
        target.consume { event ->
            events.add(event)
        }

        target.produce(event)

        while (events.isEmpty()) {
            Thread.sleep(10L)
        }
        assertThat(events).containsOnly(event)

        verify(clientMock).write("0", eventJson)
    }
}
