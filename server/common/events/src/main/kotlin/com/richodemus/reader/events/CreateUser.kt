package com.richodemus.reader.events

import com.richodemus.reader.dto.PasswordHash
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username

class CreateUser(val userId: UserId, val username: Username, val password: PasswordHash) : Event(type = EventType.CREATE_USER) {
    override fun toString() = "Create user $username"
}
