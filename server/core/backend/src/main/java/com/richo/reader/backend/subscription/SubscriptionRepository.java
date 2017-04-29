package com.richo.reader.backend.subscription;

import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.model.User;
import com.richodemus.reader.dto.Username;

public interface SubscriptionRepository
{
	User find(Username username) throws NoSuchUserException;

	void update(User user);
}
