package com.richodemus.autoplaylist.event

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.event.gcs.GoogleCloudStorage
import com.richodemus.autoplaylist.event.gcs.GoogleCloudStorageAdapter
import com.richodemus.autoplaylist.dto.events.Event
import com.richodemus.autoplaylist.dto.events.EventId
import com.richodemus.autoplaylist.eventstore.EventStore
import com.richodemus.autoplaylist.dto.events.EventType
import com.richodemus.autoplaylist.dto.events.UserCreated
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.util.UUID
import java.util.concurrent.Executors

class ReplayingEventStoreTest
    : CoroutineScope {
    override val coroutineContext = Dispatchers.Default
    private val eventJson = "{\"id\":\"2b041378-66af-4cb0-a2b0-186fc0bdc139\",\"type\":\"USER_CREATED\",\"timestamp\":\"2018-08-29T11:27:14.623013Z\",\"userId\":\"2b041378-66af-4cb0-a2b0-186fc0bdc139\",\"spotifyUserId\":\"user\",\"refreshToken\":\"refresh-token\"}".trimIndent().toByteArray()

    private val event = UserCreated(
            EventId(UUID.fromString("2b041378-66af-4cb0-a2b0-186fc0bdc139")),
            EventType.USER_CREATED,
            "2018-08-29T11:27:14.623013Z",
            UserId(UUID.fromString("2b041378-66af-4cb0-a2b0-186fc0bdc139")),
            SpotifyUserId("user"),
            RefreshToken("refresh-token")
    )

    private lateinit var registryMock: MeterRegistry

    @Before
    fun setUp() {
        registryMock = mock {
            on { counter("events") } doReturn mock<Counter> {}
        }
    }

    @Test
    fun `Add Consumer after event is produced`() {
        val clientMock = mock<GoogleCloudStorage> {
            on { read() } doReturn emptyList<Pair<Long, Deferred<ByteArray>>>()
        }

        val target = ReplayingEventStore(GoogleCloudStorageAdapter(EventSerde(), clientMock), registryMock)

        target.produce(event)

        val result = runBlocking { target.readEvents(1).await() }

        assertThat(result).containsOnly(event)
    }

    @Test
    fun `Add Consumer before event is produced`() {
        val clientMock = mock<GoogleCloudStorage> {
            on { read() } doReturn emptyList<Pair<Long, Deferred<ByteArray>>>()
        }

        val target = ReplayingEventStore(GoogleCloudStorageAdapter(EventSerde(), clientMock), registryMock)

        val resultAsync = target.readEvents(1)

        target.produce(event)

        val result = runBlocking { resultAsync.await() }
        assertThat(result).containsOnly(event)
    }

    @Test
    fun `Create store with persisted events`() {
        val clientMock = mock<GoogleCloudStorage> {
            on { read() } doReturn listOf(0L to async {
                eventJson
            })
        }

        val target = ReplayingEventStore(GoogleCloudStorageAdapter(EventSerde(), clientMock), registryMock)

        val result = runBlocking { target.readEvents(1).await() }

        assertThat(result).containsOnly(event)
    }

    @Test
    fun `Produce event`() {
        val clientMock = mock<GoogleCloudStorage> {
            on { read() } doReturn emptyList<Pair<Long, Deferred<ByteArray>>>()
        }

        val target = ReplayingEventStore(GoogleCloudStorageAdapter(EventSerde(), clientMock), registryMock)

        target.produce(event)

        verify(clientMock).write("0", eventJson)
    }

    @Test
    fun `Periodically add multiple consumers while producing`() {
        val executor = Executors.newCachedThreadPool()
        val dispatcher = executor.asCoroutineDispatcher()
        try {
            runBlocking {
                val clientMock = mock<GoogleCloudStorage> {
                    on { read() } doReturn emptyList<Pair<Long, Deferred<ByteArray>>>()
                }

                val events = (1..5_000).map { event.copy(timestamp = it.toString()) }
                val target = ReplayingEventStore(GoogleCloudStorageAdapter(EventSerde(), clientMock), registryMock)


                launch(dispatcher) {
                    events.forEach { event ->
                        target.produce(event)
                        delay(1)
                    }
                }
                val results = (1..100).map { index ->
                    delay(index.toLong())
                    target.readEvents(events.size)
                }.map { it.await() }
                results.forEach { assertThat(it).isEqualTo(events) }
            }
        } finally {
            executor.shutdown()
        }
    }

    private fun EventStore.readEvents(
            count: Int,
            dispatcher: CoroutineDispatcher = Dispatchers.Default
    ): Deferred<List<Event>> {
        var events = listOf<Event>()
        this.consume { events += it }
        return async(dispatcher) {
            while (events.size < count) {
                delay(10)
            }
            return@async events
        }
    }
}
