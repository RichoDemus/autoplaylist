package com.richodemus.reader.user_service

import com.richodemus.reader.dto.Password
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test

class UserServiceTest {
    private val username = Username("richodemus")
    private val password = Password("password")

    private var target: UserService? = null
    private fun target() = target!!

    @Before
    fun setUp() {
        target = UserService(InMemoryEventStore())
    }

    @Test
    fun `should return null if user doesnt exist`() {
        assertThat(target().find(Username("unknown"))).isNull()
    }

    @Test
    fun `Create User`() {
        val createdId = target().create(username, password)

        val result = target().find(username)!!

        assertThat(result.id).isEqualTo(createdId)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception if creating user that already exists`() {
        target().create(username, password)
        target().create(username, password)
    }

    @Test
    fun `User should have the right password`() {
        target().create(username, password)
        val result = target().passwordValid(username, password)
        assertThat(result).isTrue()
    }

    @Test
    fun `The wrong password should not be valid`() {
        target().create(username, password)

        val result = target().passwordValid(username, Password(password.value.reversed()))
        assertThat(result).isFalse()
    }

    @Test
    fun `A user that doesn't exist shouldn't have a valid passord`() {
        val result = target().passwordValid(username, password)
        assertThat(result).isFalse()
    }

    @Test
    fun `Should be possible to change password`() {
        val userId = target().create(username, Password("old password"))
        target().changePassword(userId, password)

        val result = target().passwordValid(username, password)
        assertThat(result).isTrue()
    }

    @Test
    fun `Should not be possible to change the password of a user that doesn't exist`() {
        assertThatThrownBy { target().changePassword(UserId("asdasd"), Password("asd")) }.isInstanceOf(IllegalStateException::class.java).hasMessageContaining("No user with id")
    }

    @Test
    fun `After changing password, the old password should not be valid`() {
        val oldPassword = Password("old password")
        val userId = target().create(username, oldPassword)
        target().changePassword(userId, password)

        val result = target().passwordValid(username, oldPassword)
        assertThat(result).isFalse()
    }
}
