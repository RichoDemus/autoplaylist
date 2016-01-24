package com.richo.reader.backend.user;

import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.model.User;

public interface UserPersister
{
	User get(String username) throws NoSuchUserException;

	void update(User user);

	boolean isPasswordValid(String username, String password);
}
