package com.richodemus.reader.events

import com.richodemus.reader.dto.EventId
import com.richodemus.reader.dto.PasswordHash
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username

class CreateUser(eventId: EventId, val id: UserId, val username: Username, val password: PasswordHash) : Event(eventId) {
    init {
        Pair(1, 2) // This is just here so stdlib is used for something...
    }

    override fun toString() = "Create user $username"
}
