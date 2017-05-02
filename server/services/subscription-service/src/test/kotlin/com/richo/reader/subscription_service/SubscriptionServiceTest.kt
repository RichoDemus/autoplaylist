package com.richo.reader.subscription_service

import com.codahale.metrics.MetricRegistry
import com.richodemus.reader.dto.EventId
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.PasswordHash
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import com.richodemus.reader.events.CreateUser
import com.richodemus.reader.label_service.InMemoryEventStore
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.util.UUID


class SubscriptionServiceTest {
    private val id = UserId("id")
    private val username = Username("richodemus")
    private val password = PasswordHash("password")
    private val user = User(id, username, mutableMapOf(), 0L, listOf())
    private val ylvis = FeedId("ylvis")
    private val ERB = FeedId("ERB")
    private val coolVideo = ItemId("some_video")

    private var target: SubscriptionService? = null
    private fun target() = target!!

    private var eventStore : EventStore? = null
    private fun eventStore() = eventStore!!

    @Before
    fun setUp() {
        eventStore = InMemoryEventStore()
        target = SubscriptionService(FileSystemPersistence(saveRoot = "build/saveRoots/${UUID.randomUUID()}"), eventStore(), MetricRegistry())
    }

    @Test
    fun `Create User`() {
        eventStore().add(CreateUser(EventId(), id, username, password))

        val result = target().get(id)

        val expected = User(id)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `Subscribe to feed`() {
        eventStore().add(CreateUser(EventId(), id, username, password))

        target().subscribe(id, ERB)

        val result = target().get(id)!!

        assertThat(result.feeds.keys).containsOnly(ERB)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should not be possible for a non-existing user to subscribe`() {
        target().subscribe(id, ylvis)
    }

    @Test
    fun `Watch item`() {
        eventStore().add(CreateUser(EventId(), id, username, password))

        target().subscribe(id, ERB)
        target().markAsRead(id, ERB, coolVideo)

        val result = target().get(id)!!

        assertThat(result.feeds[ERB]).containsOnly(coolVideo)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should not be possible for a non-existing user to watch an item`() {
        target().markAsRead(id, ERB, coolVideo)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should not be possible for a user to watch an item in a non-subscribed feed`() {
        eventStore().add(CreateUser(EventId(), id, username, password))
        target().markAsRead(id, ERB, coolVideo)
    }

    @Test
    fun `Unwatch item`() {
        eventStore().add(CreateUser(EventId(), id, username, password))

        target().subscribe(id, ERB)
        target().markAsRead(id, ERB, coolVideo)
        target().markAsUnread(id, ERB, coolVideo)

        val result = target().get(id)!!

        assertThat(result.feeds[ERB]).isEmpty()
    }

    @Test(expected = IllegalStateException::class)
    fun `Should not be possible for a non-existing user to unwatch an item`() {
        target().markAsUnread(id, ERB, coolVideo)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should not be possible for a user to unwatch an item in a non-subscribed feed`() {
        eventStore().add(CreateUser(EventId(), id, username, password))
        target().markAsUnread(id, ERB, coolVideo)
    }
}
