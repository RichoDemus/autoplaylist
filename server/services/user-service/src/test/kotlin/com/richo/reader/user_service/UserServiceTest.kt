package com.richo.reader.user_service

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.Password
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.util.UUID


class UserServiceTest {
    private val id = UserId("id")
    private val username = Username("richodemus")
    private val password = Password("password")
    private val user = User(id, username, emptyMap(), 0L, listOf())
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
        assertThat(target().find(Username("unknown"))).isNull()
    }

    @Test
    fun `Create User`() {
        val createdId = target().create(username, password)

        val result = target().find(username)

        val expected = User(createdId, username, emptyMap(), 0L, listOf())

        assertThat(result).isEqualTo(expected)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception if creating user that already exists`() {
        target().create(username, password)
        target().create(username, password)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should not be possible to update a non-existing user`() {
        target().update(user)
    }

    @Test
    fun `Should be possible to update a user`() {
        val createdId = target().create(username, password)
        target().update(User(createdId, username, mapOf(Pair(ERB, listOf<ItemId>())), 0L, listOf()))
        assertThat(target().find(user.name)?.feeds).containsOnlyKeys(ERB)

        val userv2 = User(createdId, username, mapOf(Pair(ERB, listOf<ItemId>()), Pair(ylvis, listOf<ItemId>())), 0L, listOf())
        target().update(userv2)

        val result = target().find(user.name)
        assertThat(result!!.feeds).containsOnlyKeys(ylvis, ERB)
    }

    @Test
    fun `Two consecutive gets should not both hit the filesystem`() {
        val filesystemMock = mock<FileSystemPersistence> {
            on { get(any()) } doReturn user
        }

        target = UserService(filesystemMock)

        target().find(Username("hello"))
        target().find(Username("hello"))
        verify(filesystemMock, times(1)).get(Username("hello"))
    }

    @Test
    fun `User should have the right password`() {
        target().create(username, password)
        val result = target().isPasswordValid(username, password)
        assertThat(result).isTrue()
    }

    @Test
    fun `A password should not be valid if none is set`() {
        val result = target().isPasswordValid(username, password)
        assertThat(result).isFalse()
    }

    @Test
    fun `Should be possible to change password`() {
        target().create(username, Password("old password"))
        target().updatePassword(username, password)

        val result = target().isPasswordValid(username, password)
        assertThat(result).isTrue()
    }

    @Test
    fun `The wrong password should not be valid`() {
        target().create(username, password)

        val result = target().isPasswordValid(username, Password(password.value.reversed()))
        assertThat(result).isFalse()
    }

    @Test
    fun `After changing password, the old password should not be valid`() {
        val oldPassword = Password("old password")
        target().create(username, oldPassword)
        target().updatePassword(username, password)

        val result = target().isPasswordValid(username, oldPassword)
        assertThat(result).isFalse()
    }
}
