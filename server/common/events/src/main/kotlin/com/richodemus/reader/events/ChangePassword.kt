package com.richodemus.reader.events

import com.richodemus.reader.dto.EventId
import com.richodemus.reader.dto.PasswordHash
import com.richodemus.reader.dto.UserId

class ChangePassword(eventId: EventId, val userId: UserId, val password: PasswordHash) : Event(eventId, EventType.CHANGE_PASSWORD) {
    override fun toString() = "Change password of user $userId"
}
