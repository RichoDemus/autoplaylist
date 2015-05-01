package com.richo.reader.backend.user;

import com.google.api.client.util.Maps;
import com.richo.reader.backend.model.Feed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class SimpleHashmapUserStorage implements UserStorage
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Map<String, User> users;

	SimpleHashmapUserStorage()
	{
		users = Maps.newHashMap();
		final User user = new User("RichoDemus", Collections.singletonList(new Feed("", "RichoDemus")).stream().collect(Collectors.toSet()));
		users.put(user.getName(), user);
	}

	@Override
	public Optional<User> get(String username)
	{
		return Optional.ofNullable(users.get(username));
	}
}
