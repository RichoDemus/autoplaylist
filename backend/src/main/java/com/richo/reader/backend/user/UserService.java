package com.richo.reader.backend.user;

import com.richo.reader.backend.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;

public class UserService implements UserPersister
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final UserPersister inMemoryPersister;
	private final UserPersister fileSystemPersister;

	@Inject
	UserService(@Named("InMemory") UserPersister users, @Named("FileSystem") UserPersister fileSystemPersister)
	{
		this.inMemoryPersister = users;
		this.fileSystemPersister = fileSystemPersister;
	}

	@Override
	public Optional<User> get(String username)
	{
		return Optional.ofNullable(inMemoryPersister.get(username).orElseGet(() ->
		{
			final Optional<User> fromFileSystem = fileSystemPersister.get(username);
			if (!fromFileSystem.isPresent())
			{
				return null;
			}

			inMemoryPersister.update(fromFileSystem.get());
			return fromFileSystem.get();
		}));
	}

	@Override
	public void update(User user)
	{
		inMemoryPersister.update(user);
		fileSystemPersister.update(user);
	}

}
