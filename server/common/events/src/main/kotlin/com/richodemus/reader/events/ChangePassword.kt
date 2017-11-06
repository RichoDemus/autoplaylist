package com.richodemus.reader.events

import com.richodemus.reader.dto.PasswordHash
import com.richodemus.reader.dto.UserId

class ChangePassword(val userId: UserId, val password: PasswordHash) : Event(type = EventType.CHANGE_PASSWORD) {
    override fun toString() = "Change password of user $userId"
}
