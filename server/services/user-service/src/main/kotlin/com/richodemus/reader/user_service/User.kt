package com.richodemus.reader.user_service

import com.richodemus.reader.dto.PasswordHash
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import com.richodemus.reader.events.ChangePassword
import com.richodemus.reader.events.Event

data class User(val id: UserId, val username: Username, val password: PasswordHash) {
    internal fun process(evt: Event): User {
        if (evt is ChangePassword && evt.userId == id) {
            return this.copy(password = evt.password)
        }
        return this
    }
}
