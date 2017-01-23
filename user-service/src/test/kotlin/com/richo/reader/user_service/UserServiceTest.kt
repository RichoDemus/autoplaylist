package com.richo.reader.user_service

import com.nhaarman.mockito_kotlin.*
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.Password
import com.richodemus.reader.dto.Username
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.util.*


class UserServiceTest {
    private val username = Username("richodemus")
    private val password = Password("password")
    private val user = User(username, emptyMap(), 0L, listOf())
    private val ylvis = FeedId("ylvis")
    private val ERB = FeedId("ERB")

    private var target: UserService? = null
    private fun target() = target!!

    @Before
    fun setUp() {
        target = UserService(FileSystemPersistence(saveRoot = "build/saveRoots/${UUID.randomUUID()}"))
    }

    @Test
    fun `should return null if user doesnt exist`() {
        assertThat(target().get(Username("unknown"))).isNull()
    }

    @Test
    fun `Creating a user should make it getable`() {
        target().update(user)

        assertThat(target().get(user.name)).isEqualTo(user)
    }

    @Test
    fun `Should be possible to update a user`() {
        target().update(User(username, mapOf(Pair(ERB, listOf<ItemId>())), 0L, listOf()))
        assertThat(target().get(user.name)?.feeds).containsOnlyKeys(ERB)

        val userv2 = User(username, mapOf(Pair(ERB, listOf<ItemId>()), Pair(ylvis, listOf<ItemId>())), 0L, listOf())
        target().update(userv2)

        val result = target().get(user.name)
        assertThat(result!!.feeds).containsOnlyKeys(ylvis, ERB)
    }

    @Test
    fun `Two consecutive gets should not both hit the filesystem`() {
        val filesystemMock = mock<FileSystemPersistence> {
            on { get(any()) } doReturn user
        }

        target = UserService(filesystemMock)

        target().get(Username("hello"))
        target().get(Username("hello"))
        verify(filesystemMock, times(1)).get(Username("hello"))
    }

    @Test
    fun `A password should not be valid if none is set`() {
        val result = target().isPasswordValid(username, password)
        assertThat(result).isFalse()
    }

    @Test
    fun `Should be possible to set password`() {
        target().updatePassword(username, password)

        val result = target().isPasswordValid(username, password)
        assertThat(result).isTrue()
    }

    @Test
    fun `The wrong password should not be valid`() {
        target().updatePassword(username, password)

        val result = target().isPasswordValid(username, Password(password.value.reversed()))
        assertThat(result).isFalse()
    }
}
