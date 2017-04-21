package com.richo.reader.subscription_service

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.richodemus.reader.dto.EventId
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.PasswordHash
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import com.richodemus.reader.events.CreateUser
import com.richodemus.reader.events.Event
import io.reactivex.subjects.PublishSubject
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.util.UUID


class SubscriptionServiceTest {
    private val id = UserId("id")
    private val username = Username("richodemus")
    private val password = PasswordHash("password")
    private val user = User(id, username, emptyMap(), 0L, listOf())
    private val ylvis = FeedId("ylvis")
    private val ERB = FeedId("ERB")

    private var target: SubscriptionService? = null
    private fun target() = target!!

    private var eventStoreMock: EventStore? = null
    private fun eventStoreMock() = eventStoreMock!!

    private var subject: PublishSubject<Event>? = null
    private fun subject() = subject!!

    @Before
    fun setUp() {
        subject = PublishSubject.create<Event>()
        eventStoreMock = mock<EventStore> {
            on { observe() } doReturn subject()
        }
        target = SubscriptionService(FileSystemPersistence(saveRoot = "build/saveRoots/${UUID.randomUUID()}"), eventStoreMock())
    }

    @Test
    fun `should return null if user doesnt exist`() {
        assertThat(target().find(Username("unknown"))).isNull()
    }

    @Test
    fun `Create User`() {
        subject().onNext(CreateUser(EventId(UUID.randomUUID()), id, username, password))

        val result = target().find(username)

        val expected = User(id, username, emptyMap(), 0L, listOf())

        assertThat(result).isEqualTo(expected)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should not be possible to update a non-existing user`() {
        target().update(user)
    }

    @Test
    fun `Should be possible to update a user`() {
        subject().onNext(CreateUser(EventId(UUID.randomUUID()), id, username, password))
        target().update(User(id, username, mapOf(Pair(ERB, listOf<ItemId>())), 0L, listOf()))
        assertThat(target().find(user.name)?.feeds).containsOnlyKeys(ERB)

        val userv2 = User(id, username, mapOf(Pair(ERB, listOf<ItemId>()), Pair(ylvis, listOf<ItemId>())), 0L, listOf())
        target().update(userv2)

        val result = target().find(user.name)
        assertThat(result!!.feeds).containsOnlyKeys(ylvis, ERB)
    }

    @Test
    fun `Two consecutive gets should not both hit the filesystem`() {
        val filesystemMock = mock<FileSystemPersistence> {
            on { get(any()) } doReturn user
        }

        target = SubscriptionService(filesystemMock, eventStoreMock())

        target().find(Username("hello"))
        target().find(Username("hello"))
        verify(filesystemMock, times(1)).get(Username("hello"))
    }
}
