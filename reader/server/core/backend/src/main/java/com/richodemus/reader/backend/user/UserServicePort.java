package com.richodemus.reader.backend.user;

import com.richodemus.reader.backend.exception.NoSuchUserException;
import com.richodemus.reader.dto.UserId;
import com.richodemus.reader.dto.Username;
import com.richodemus.reader.user_service.User;
import com.richodemus.reader.user_service.UserService;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserServicePort implements UserRepository {
    private final UserService userService;

    UserServicePort(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserId getUserId(final Username username) {
        return Optional.ofNullable(userService.find(username))
                .map(User::getId)
                .orElseThrow(() -> new NoSuchUserException("No such user: " + username));
    }
}
