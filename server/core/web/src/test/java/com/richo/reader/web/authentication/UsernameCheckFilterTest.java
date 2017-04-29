package com.richo.reader.web.authentication;

import com.richodemus.dropwizard.jwt.AuthenticationManager;
import com.richodemus.reader.dto.PasswordHash;
import com.richodemus.reader.dto.UserId;
import com.richodemus.reader.dto.Username;
import com.richodemus.reader.user_service.User;
import com.richodemus.reader.user_service.UserService;
import org.glassfish.jersey.server.ContainerRequest;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsernameCheckFilterTest
{
	private static final String VALID_USER_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjoidXNlcm5hbWUiLCJyb2xlIjoidXNlciJ9.xciSe5aT_-LjjSs7sqtyo9J2wuqR4Bl4kb4scjcSTvQ";
	private static final String EXISTING_VALID_USER = "username";
	private static final String WRONG_USERNAME = "not-username";
	private UsernameCheckFilter target;
	private UserService userServiceMock;

	@Before
	public void setUp() throws Exception
	{
		userServiceMock = mock(UserService.class);
		when(userServiceMock.find(new Username(EXISTING_VALID_USER))).thenReturn(new User(new UserId("id"), new Username(EXISTING_VALID_USER), new PasswordHash("asd")));
		target = new UsernameCheckFilter(new AuthenticationManager(null, null, "secret_used_for_testing"), userServiceMock);
	}

	@Test
	public void shouldNotBlockIfNotAccessingUserResource() throws Exception
	{
		final ContainerRequest mock = createMock("admin/", VALID_USER_TOKEN);
		target.filter(mock);
		verify(mock, times(0)).abortWith(any());
	}

	@Test
	public void shouldDoNothingWhenUsernamesMatch() throws Exception
	{
		final ContainerRequest mock = createMockByUsername(EXISTING_VALID_USER, VALID_USER_TOKEN);
		target.filter(mock);
		verify(mock, times(0)).abortWith(any());
	}

	@Test
	public void shouldDoNothingWhenPathIsWithoutUsername() throws Exception
	{
		final ContainerRequest mock = createMock("users/", VALID_USER_TOKEN);
		target.filter(mock);
		verify(mock, times(0)).abortWith(any());
	}

	@Test
	public void shouldDoNothingWhenNoToken() throws Exception
	{
		final ContainerRequest mock = createMockByUsername(EXISTING_VALID_USER, VALID_USER_TOKEN);
		target.filter(mock);
		verify(mock, times(0)).abortWith(any());
	}

	@Test
	public void shouldAbortWhenUsernamesDontMatch() throws Exception
	{
		final ContainerRequest mock = createMockByUsername(WRONG_USERNAME, VALID_USER_TOKEN);
		target.filter(mock);
		verify(mock).abortWith(any());
	}

	@Test
	public void shouldAbortWhenUserDoesntExist() throws Exception
	{
		when(userServiceMock.find(new Username(EXISTING_VALID_USER))).thenReturn(null);
		final ContainerRequest mock = createMockByUsername(EXISTING_VALID_USER, VALID_USER_TOKEN);
		target.filter(mock);
		verify(mock).abortWith(any());
	}

	private ContainerRequest createMockByUsername(String username, String token) throws URISyntaxException
	{
		return createMock("users/" + username + "/feeds", token);
	}

	private ContainerRequest createMock(final String path, String token)
	{
		final ContainerRequest requestContextMock = mock(ContainerRequest.class);
		when(requestContextMock.getPath(anyBoolean())).thenReturn(path);
		if (token != null)
		{
			when(requestContextMock.getHeaderString("x-token-jwt")).thenReturn(token);
		}
		return requestContextMock;
	}


}
