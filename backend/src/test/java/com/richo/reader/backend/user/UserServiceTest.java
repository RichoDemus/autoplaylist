package com.richo.reader.backend.user;

import com.richo.reader.backend.exception.NoSuchUserException;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserServiceTest
{
	private static final String NON_EXISTING_USER = "non_existing_user";
	private UserService target;
	private UserPersister memoryMock;
	private UserPersister fileSystemMock;

	@Before
	public void setUp() throws Exception
	{
		memoryMock = mock(UserPersister.class);
		fileSystemMock = mock(UserPersister.class);
		target = new UserService(memoryMock, fileSystemMock);
	}

	@Test(expected = NoSuchUserException.class)
	public void getShouldThrowNoSuchUserExceptionIfUserDoesntExist() throws Exception
	{
		when(memoryMock.get(NON_EXISTING_USER)).thenThrow(new NoSuchUserException(""));
		when(fileSystemMock.get(NON_EXISTING_USER)).thenThrow(new NoSuchUserException(""));

		target.get(NON_EXISTING_USER);
	}
}