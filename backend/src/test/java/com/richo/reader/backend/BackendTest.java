package com.richo.reader.backend;

import com.richo.reader.backend.exception.NoSuchUserException;
import com.richo.reader.backend.user.UserService;
import com.richo.reader.backend.youtube.YoutubeChannelService;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BackendTest
{
	private static final String NON_EXISTING_USER = "non_existing_user";
	private Backend target;
	private UserService userService;
	private YoutubeChannelService channelService;

	@Before
	public void setUp() throws Exception
	{
		channelService = mock(YoutubeChannelService.class);
		userService = mock(UserService.class);
		target = new Backend(userService, channelService);
	}

	@Test(expected = NoSuchUserException.class)
	public void getFeedsShouldThrowNoSuchUserExceptionIfUserDoesntExist() throws Exception
	{
		when(userService.get(NON_EXISTING_USER)).thenThrow(new NoSuchUserException(""));

		target.getFeeds(NON_EXISTING_USER);
	}
}