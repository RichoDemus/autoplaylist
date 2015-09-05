package com.richo.reader.backend.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Lists;
import com.richo.reader.backend.exception.NoSuchUserException;
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
	public User get(String username) throws NoSuchUserException
	{
		try
		{
			final User user = new ObjectMapper().readValue(new File(saveRoot + "/users/" + username + "/data.json"), User.class);
			return Optional.of(user).map(this::withoutNulls).get();
		}
		catch (IOException e)
		{
			logger.warn("Unable to load user: {}", username, e);
			throw new NoSuchUserException("Unable to load user: " + username);
		}
	}

	/**
	 * Since this structure changes alot, this object might have null values, lets just make sure there are none
	 * todo remove this when either User is stable or when we have api version support here
	 */
	private User withoutNulls(User user)
	{
		if (user.getLabels() != null)
		{
			return user;
		}
		return new User(user.getName(), user.getNextLabelId(), user.getFeeds(), Lists.newArrayList());
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
