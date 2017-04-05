package com.richo.reader.web.authentication;

import com.richodemus.dropwizard.jwt.AuthenticationManager;
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
	private UsernameCheckFilter target;

	@Before
	public void setUp() throws Exception
	{
		target = new UsernameCheckFilter(new AuthenticationManager(null, null, "secret_used_for_testing"));
	}

	@Test
	public void shouldNotBlockIfNotAccessingUserResource() throws Exception
	{
		final ContainerRequest mock = createMock("/api/admin/", VALID_USER_TOKEN);
		target.filter(mock);
		verify(mock, times(0)).abortWith(any());
	}

	@Test
	public void shouldDoNothingWhenUsernamesMatch() throws Exception
	{
		final ContainerRequest mock = createMockByUsername("username", VALID_USER_TOKEN);
		target.filter(mock);
		verify(mock, times(0)).abortWith(any());
	}

	@Test
	public void shouldDoNothingWhenPathIsWithoutUsername() throws Exception
	{
		final ContainerRequest mock = createMock("/api/users/", VALID_USER_TOKEN);
		target.filter(mock);
		verify(mock, times(0)).abortWith(any());
	}

	@Test
	public void shouldDoNothingWhenNoToken() throws Exception
	{
		final ContainerRequest mock = createMockByUsername("username", null);
		target.filter(mock);
		verify(mock, times(0)).abortWith(any());
	}

	@Test
	public void shouldAbortWhenUsernamesDontMatch() throws Exception
	{
		final ContainerRequest mock = createMockByUsername("not-username", VALID_USER_TOKEN);
		target.filter(mock);
		verify(mock).abortWith(any());
	}

	private ContainerRequest createMockByUsername(String username, String token) throws URISyntaxException
	{
		return createMock("/api/users/" + username + "/feeds", token);
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
