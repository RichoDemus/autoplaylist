package com.richodemus.reader.backend.user

import com.richodemus.reader.backend.exception.NoSuchUserException
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import com.richodemus.reader.user_service.User
import com.richodemus.reader.user_service.UserService
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class UserServicePort internal constructor(private val userService: UserService) : UserRepository {
    override fun getUserId(username: Username): UserId {
        return Optional.ofNullable(userService.find(username))
                .map(User::id)
                .orElseThrow { NoSuchUserException("No such user: $username") }
    }
}
