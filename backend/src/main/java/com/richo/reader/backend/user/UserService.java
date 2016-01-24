package com.richo.reader.backend.user;

import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.model.User;

import javax.inject.Inject;
import javax.inject.Named;

public class UserService implements UserPersister
{
	private final UserPersister inMemoryPersister;
	private final UserPersister fileSystemPersister;

	@Inject
	UserService(@Named("InMemory") UserPersister users, @Named("FileSystem") UserPersister fileSystemPersister)
	{
		this.inMemoryPersister = users;
		this.fileSystemPersister = fileSystemPersister;
	}

	@Override
	public User get(String username) throws NoSuchUserException
	{
		try
		{
			return inMemoryPersister.get(username);
		}
		catch (NoSuchUserException e)
		{
			return fileSystemPersister.get(username);
		}
	}

	@Override
	public void update(User user)
	{
		inMemoryPersister.update(user);
		fileSystemPersister.update(user);
	}

	@Override
	public boolean isPasswordValid(String username, String password)
	{
		return fileSystemPersister.isPasswordValid(username, password);
	}

}
