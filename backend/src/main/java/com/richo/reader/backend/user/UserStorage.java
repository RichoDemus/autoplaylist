package com.richo.reader.backend.user;

import java.util.Optional;

public interface UserStorage
{
	Optional<User> get(String username);
}
