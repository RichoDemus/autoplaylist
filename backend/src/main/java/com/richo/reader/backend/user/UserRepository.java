package com.richo.reader.backend.user;

import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.model.User;
import com.richodemus.reader.dto.Password;
import com.richodemus.reader.dto.UserId;
import com.richodemus.reader.dto.Username;

public interface UserRepository
{
	User get(UserId username) throws NoSuchUserException;

	void update(User user);

	boolean isPasswordValid(UserId username, String password);

	void updatePassword(UserId username, String password);

	UserId create(Username username, Password password);
}
