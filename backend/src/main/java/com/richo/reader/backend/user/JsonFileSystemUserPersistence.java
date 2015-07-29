package com.richo.reader.backend.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;


public class JsonFileSystemUserPersistence implements UserPersister
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final String saveRoot;

	@Inject
	JsonFileSystemUserPersistence(@Named("saveRoot") String saveRoot)
	{
		this.saveRoot = saveRoot;
	}

	@Override
	public User get(String username) throws NoSuchUserException
	{
		try
		{
			return new ObjectMapper().readValue(new File(saveRoot + "/users/" + username + "/data.json"), User.class);
		}
		catch (IOException e)
		{
			logger.warn("Unable to load user: {}", username, e);
			throw new NoSuchUserException("Unable to load user: " + username);
		}
	}

	@Override
	public void update(User user)
	{
		try
		{
			final String path = saveRoot + "/users/" + user.getName();
			final boolean success = new File(path).mkdirs();
			logger.trace("Creating {} successful: {}", path, success);
			new ObjectMapper().writeValue(new File(path + "/data.json"), user);
		}
		catch (IOException e)
		{
			//todo fix
			e.printStackTrace();
		}
	}
}
