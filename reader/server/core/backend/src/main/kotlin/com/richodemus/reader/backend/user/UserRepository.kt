package com.richodemus.reader.backend.user

import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username

interface UserRepository {
    fun getUserId(username: Username): UserId
}
