package com.richo.reader.test;

import com.richo.reader.test.pages.LoginPage;
import com.richo.reader.test.util.DropwizardContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTestIT
{
	private static final String USERNAME = "richodemus";
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
		loginPage.login(USERNAME);

		assertThat(loginPage.isLoggedIn()).isFalse();
	}

	@Test
	public void shouldCreateUser() throws Exception
	{
		loginPage.createUser(USERNAME);
	}

	@Test
	public void shouldNotCreateUserWithInvalidInviteCode() throws Exception
	{
		final int status = loginPage.createUser(USERNAME, "wrong code");
		assertThat(status).isEqualTo(403);

		loginPage.login(USERNAME);
		assertThat(loginPage.isLoggedIn()).isFalse();
	}

	@Test
	public void shouldLoginUser() throws Exception
	{
		loginPage.createUser(USERNAME);

		loginPage.login(USERNAME);

		assertThat(loginPage.isLoggedIn()).isTrue();
	}

	@Test
	public void usernamesShouldBeCaseInsensitive() throws Exception
	{
		loginPage.createUser("UPPERCASE");
		loginPage.login("uppercase");
		assertThat(loginPage.isLoggedIn()).isTrue();

		final LoginPage loginPage2 = new LoginPage(target.getHttpPort());
		loginPage2.createUser("lowercase");
		loginPage2.login("LOWERCASE");
		assertThat(loginPage2.isLoggedIn()).isTrue();
	}

	@Test
	public void shouldNotLoginUserWithInvalidPassword() throws Exception
	{
		loginPage.createUser(USERNAME);

		loginPage.login(USERNAME, "not_the_right_password");

		assertThat(loginPage.isLoggedIn()).isFalse();
	}
}
