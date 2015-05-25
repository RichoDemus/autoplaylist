package com.richo.reader.backend.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.richo.reader.backend.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.util.Optional;


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
	public Optional<User> get(String username)
	{
		try
		{
			return Optional.ofNullable(new ObjectMapper().readValue(new File(saveRoot + "/users/" + username + "/data.json"), User.class));
		}
		catch (IOException e)
		{
			logger.warn("Unable to load channel: {}", username, e);
			return Optional.empty();
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
			e.printStackTrace();
		}
	}
}
