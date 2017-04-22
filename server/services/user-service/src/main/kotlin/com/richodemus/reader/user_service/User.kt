package com.richodemus.reader.user_service

import com.richodemus.reader.dto.PasswordHash
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username

data class User(val id: UserId, val username: Username, var password: PasswordHash)
