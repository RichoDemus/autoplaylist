package com.richo.reader.web.authentication;

import org.glassfish.jersey.server.ContainerRequest;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsernameCheckFilterTest
{
	public static final String VALID_USER_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjoidXNlcm5hbWUiLCJyb2xlIjoidXNlciJ9.xciSe5aT_-LjjSs7sqtyo9J2wuqR4Bl4kb4scjcSTvQ";
	private UsernameCheckFilter target;

	@Before
	public void setUp() throws Exception
	{
		target = new UsernameCheckFilter();
	}

	@Test
	public void shouldDoNothingWhenUsernamesMatch() throws Exception
	{
		final ContainerRequest mock = createMock("username", VALID_USER_TOKEN);
		target.filter(mock);
		verify(mock, times(0)).abortWith(any());
	}

	@Test
	public void shouldDoNothingWhenPathIsWithoutUsername() throws Exception
	{
		final ContainerRequest mock = createMock(new URI("http://localhost:8080/api/users/"), VALID_USER_TOKEN);
		target.filter(mock);
		verify(mock, times(0)).abortWith(any());
	}

	@Test
	public void shouldDoNothingWhenNoToken() throws Exception
	{
		final ContainerRequest mock = createMock("username", null);
		target.filter(mock);
		verify(mock, times(0)).abortWith(any());
	}

	@Test
	public void shouldAbortWhenUsernamesDontMatch() throws Exception
	{
		final ContainerRequest mock = createMock("not-username", VALID_USER_TOKEN);
		target.filter(mock);
		verify(mock).abortWith(any());
	}

	private ContainerRequest createMock(String username, String token) throws URISyntaxException
	{
		return createMock(new URI("http://localhost:8080/api/users/" + username + "/feeds"), token);
	}

	private ContainerRequest createMock(URI uri, String token)
	{
		final ContainerRequest requestContextMock = mock(ContainerRequest.class);
		when(requestContextMock.getAbsolutePath()).thenReturn(uri);
		if (token != null)
		{
			when(requestContextMock.getHeaderString(eq("x-token-jwt"))).thenReturn(token);
		}
		return requestContextMock;
	}


}
