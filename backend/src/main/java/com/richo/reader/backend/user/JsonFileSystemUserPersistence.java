package com.richo.reader.backend.user;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.model.User;
import com.richodemus.reader.dto.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
	public User get(UserId username) throws NoSuchUserException
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
		return new User(user.getName(), user.getNextLabelId(), user.getFeeds(), new ArrayList<>());
	}

	@Override
	public void update(User user)
	{
		try
		{
			final String path = saveRoot + "/users/" + user.getName();
			final boolean success = new File(path).mkdirs();
			logger.trace("Creating {} successful: {}", path, success);
			final DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
			pp.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
			new ObjectMapper().writer(pp).writeValue(new File(path + "/data.json"), user);
		}
		catch (IOException e)
		{
			//todo fix
			e.printStackTrace();
		}
	}

	public void setPassword(UserId username, String password)
	{
		try
		{
			final String path = saveRoot + "/users/" + username;
			final boolean success = new File(path).mkdirs();
			logger.trace("Creating {} successful: {}", path, success);
			new ObjectMapper().writeValue(new File(path + "/password.json"), password);
		}
		catch (IOException e)
		{
			//todo fix
			e.printStackTrace();
		}
	}

	@Override
	public boolean isPasswordValid(UserId username, String password)
	{
		try
		{
			final String loadedPassword = new ObjectMapper().readValue(new File(saveRoot + "/users/" + username + "/password.json"), String.class);
			return password.equals(loadedPassword);
		}
		catch (IOException e)
		{
			logger.warn("Unable to find passowrd for user user: {}", username, e);
			throw new NoSuchUserException("Unable to find passowrd for user user: " + username);
		}
	}

	@Override
	public void updatePassword(final UserId username, final String password)
	{
		try
		{
			logger.debug("Changing password for user {}", username);
			new ObjectMapper().writeValue(new File(saveRoot + "/users/" + username + "/password.json"), password);
		}
		catch (IOException e)
		{
			logger.error("Unable to change password for user {}", username, e);
		}

	}
}
