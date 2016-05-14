package com.richo.reader.test;

import com.richo.reader.test.pages.LoginPage;
import com.richo.reader.test.util.DropwizardContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTestIT
{
	private DropwizardContainer target;
	private LoginPage loginPage;

	@Before
	public void setUp() throws Exception
	{
		target = new DropwizardContainer("richodemus/reader");
		loginPage = new LoginPage(target.getHttpPort());
	}

	@After
	public void tearDown() throws Exception
	{
		target.close();
	}

	@Test
	public void shouldNotBeAbleToLoginIfUserDoesntExist() throws Exception
	{
		final String username = "richodemus";

		loginPage.login(username);

		assertThat(loginPage.isLoggedIn()).isFalse();
	}

	@Test
	public void shouldCreateUser() throws Exception
	{
		loginPage.createUser("richodemus");
	}

	@Test
	public void shouldLoginUser() throws Exception
	{
		final String username = "richodemus";
		loginPage.createUser(username);

		loginPage.login(username);

		assertThat(loginPage.isLoggedIn()).isTrue();
	}

	@Test
	public void shouldNotLoginUserWithInvalidPassword() throws Exception
	{
		final String username = "richodemus";
		loginPage.createUser(username);

		loginPage.login(username, "not_the_right_password");

		assertThat(loginPage.isLoggedIn()).isFalse();
	}
}
