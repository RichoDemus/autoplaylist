package com.richo.reader.backend.user;

import com.richo.reader.backend.model.User;

import java.util.Optional;

public interface UserPersister
{
	Optional<User> get(String username);

	void update(User user);
}
