package com.richo.reader.backend.user;

import com.richo.reader.backend.exception.NoSuchUserException;
import com.richodemus.reader.dto.UserId;
import com.richodemus.reader.dto.Username;
import com.richodemus.reader.user_service.User;
import com.richodemus.reader.user_service.UserService;

import javax.inject.Inject;
import java.util.Optional;

public class UserServicePort implements UserRepository {
    private final UserService userService;

    @Inject
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
