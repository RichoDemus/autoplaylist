package com.richo.reader.subscription_service

import com.codahale.metrics.MetricRegistry
import com.richodemus.reader.common.google_cloud_storage_adapter.EventStore
import com.richodemus.reader.common.google_cloud_storage_adapter.InMemoryEventStore
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.PasswordHash
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import com.richodemus.reader.events_v2.UserCreated
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test


class SubscriptionServiceTest {
    private val id = UserId("id")
    private val username = Username("richodemus")
    private val password = PasswordHash("password")
    private val ylvis = FeedId("ylvis")
    private val ERB = FeedId("ERB")
    private val coolVideo = ItemId("some_video")

    private var target: SubscriptionService? = null
    private fun target() = target!!

    private var eventStore: EventStore? = null
    private fun eventStore() = eventStore!!

    @Before
    fun setUp() {
        eventStore = InMemoryEventStore()
        target = SubscriptionService(eventStore(), MetricRegistry())
    }

    @Test
    fun `Create User`() {
        eventStore().produce(UserCreated(id, username, password))

        val result = target().get(id)

        val expected = User(id)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `Subscribe to feed`() {
        eventStore().produce(UserCreated(id, username, password))

        target().subscribe(id, ERB)

        val result = target().get(id)!!

        assertThat(result.feeds.map { it.id }).containsOnly(ERB)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should not be possible for a non-existing user to subscribe`() {
        target().subscribe(id, ylvis)
    }

    @Test
    fun `Watch item`() {
        eventStore().produce(UserCreated(id, username, password))

        target().subscribe(id, ERB)
        target().markAsRead(id, ERB, coolVideo)

        val result = target().get(id)!!

        assertThat(result.feeds.first { it.id == ERB }.watchedItems).containsOnly(coolVideo)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should not be possible for a non-existing user to watch an item`() {
        target().markAsRead(id, ERB, coolVideo)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should not be possible for a user to watch an item in a non-subscribed feed`() {
        eventStore().produce(UserCreated(id, username, password))
        target().markAsRead(id, ERB, coolVideo)
    }

    @Test
    fun `Unwatch item`() {
        eventStore().produce(UserCreated(id, username, password))

        target().subscribe(id, ERB)
        target().markAsRead(id, ERB, coolVideo)
        target().markAsUnread(id, ERB, coolVideo)

        val result = target().get(id)!!

        assertThat(result.feeds.first { it.id == ERB }.watchedItems).isEmpty()
    }

    @Test(expected = IllegalStateException::class)
    fun `Should not be possible for a non-existing user to unwatch an item`() {
        target().markAsUnread(id, ERB, coolVideo)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should not be possible for a user to unwatch an item in a non-subscribed feed`() {
        eventStore().produce(UserCreated(id, username, password))
        target().markAsUnread(id, ERB, coolVideo)
    }
}
