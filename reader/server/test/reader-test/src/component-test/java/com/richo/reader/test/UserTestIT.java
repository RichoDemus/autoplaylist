package com.richo.reader.test;

import com.richo.reader.test.pages.LoginPage;
import com.richo.reader.test.pages.model.FeedWithoutItem;
import com.richo.reader.test.util.TestableApplication;
import com.richo.reader.test.util.TestableApplicationProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class UserTestIT
{
	private static final String USERNAME = "richodemus";
	private TestableApplication target;
	private LoginPage loginPage;

	@Before
	public void setUp() throws Exception {
		target = new TestableApplicationProvider().readerApplication();
		loginPage = new LoginPage(target.getHttpPort());
	}

	@After
	public void tearDown() {
		target.close();
	}

	@Test
	public void shouldNotBeAbleToLoginIfUserDoesntExist() {
		loginPage.login(USERNAME);

		assertThat(loginPage.isLoggedIn()).isFalse();
	}

	@Test
	public void shouldCreateUser() {
		loginPage.createUser(USERNAME);
	}

	@Test
	public void shouldNotCreateUserWithInvalidInviteCode() {
		final int status = loginPage.createUser(USERNAME, "wrong code");
		assertThat(status).isEqualTo(403);

		loginPage.login(USERNAME);
		assertThat(loginPage.isLoggedIn()).isFalse();
	}

	@Test
	public void shouldLoginUser() {
		loginPage.createUser(USERNAME);

		loginPage.login(USERNAME);

		assertThat(loginPage.isLoggedIn()).isTrue();
	}

	@Test
	public void shouldGetNewToken() {
		loginPage.createUser(USERNAME);

		loginPage.login(USERNAME);

		final String firstToken = loginPage.getToken();

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
		{
			loginPage.refreshToken();

			assertThat(loginPage.getToken()).isNotEqualTo(firstToken);
		});

		final List<FeedWithoutItem> result = loginPage.toFeedPage().getAllFeeds();

		assertThat(result).isNotNull();
	}

	@Test
	public void usernamesShouldBeCaseInsensitive() {
		loginPage.createUser("UPPERCASE");
		loginPage.login("uppercase");
		assertThat(loginPage.isLoggedIn()).isTrue();

		final LoginPage loginPage2 = new LoginPage(target.getHttpPort());
		loginPage2.createUser("lowercase");
		loginPage2.login("LOWERCASE");
		assertThat(loginPage2.isLoggedIn()).isTrue();
	}

	@Test
	public void shouldNotLoginUserWithInvalidPassword() {
		loginPage.createUser(USERNAME);

		loginPage.login(USERNAME, "not_the_right_password");

		assertThat(loginPage.isLoggedIn()).isFalse();
	}
}
