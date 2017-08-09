package com.richo.reader.test;

import com.richo.reader.test.pages.LoginPage;
import com.richo.reader.test.pages.model.FeedUrl;
import com.richo.reader.test.util.TestableApplication;
import com.richo.reader.test.util.TestableApplicationProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SecurityTestIT
{
	private static final String USERNAME = "richodemus";
	private TestableApplication youtubeMock;
	private TestableApplication target;
	private LoginPage loginPage;

	@Before
	public void setUp() throws Exception
	{
		youtubeMock = new TestableApplicationProvider().youtubeMock();
		target = new TestableApplicationProvider().readerApplication(youtubeMock.getHttpPort());
		loginPage = new LoginPage(target.getHttpPort());
	}

	@After
	public void tearDown() throws Exception
	{
		target.close();
	}

	@Test
	public void shouldNotBeAbleToAccessStuffUsingAlgNoneToken() throws Exception
	{
		// this is a token with alg: none, user: richodemus, role: user and exp: 9493827929
		final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJub25lIn0.eyJleHAiOjk0OTM4Mjc5MjgsInVzZXIiOiJyaWNob2RlbXVzIiwicm9sZSI6InVzZXIifQ.ASBDxK66U1hIPIUKCHOXjCq38UjJt0Gqf0DlZRygCRY";

		loginPage.createUser(USERNAME);

		loginPage.setUsername(USERNAME);
		loginPage.setToken(token);

		assertThatThrownBy(() -> loginPage.toFeedPage().getAllFeeds()).isInstanceOf(AssertionError.class).hasMessageContaining("Expected status code <200> doesn't match actual status code <400>.");
	}

	@Test
	public void shouldNotBePossibleToTouchOtherUsersStuff() throws Exception
	{
		final String victim = USERNAME;
		final String secondAccount = "other";


		// create users
		loginPage.createUser(victim);
		loginPage.createUser(secondAccount);

		// subscribe to a feed
		loginPage.login(victim);
		loginPage.toFeedPage().addFeed(new FeedUrl("https://www.youtube.com/user/richodemus"));

		// create a token that is valid for the other user
		final LoginPage otherUserPage = new LoginPage(target.getHttpPort());
		otherUserPage.login(secondAccount);
		final String otherUserToken = otherUserPage.getToken();

		// use the token for the other account but username of the victim
		final LoginPage hackingAttemptPage = new LoginPage(target.getHttpPort());
		hackingAttemptPage.setUsername(victim);
		hackingAttemptPage.setToken(otherUserToken);

		// attempt to get feeds for another account than the token is issued for
		assertThatThrownBy(() -> hackingAttemptPage.toFeedPage().getAllFeeds()).isInstanceOf(AssertionError.class).hasMessageContaining("Expected status code <200> doesn't match actual status code <401>.");
	}

	@Test
	public void shouldNotBePossibleToUseWithValidTokenButUnknownUserName() throws Exception
	{
		// this is a token with user: not_richodemus
		final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE0OTYwNjgzNDcsInVzZXIiOiJub3RfcmljaG9kZW11cyIsInJvbGUiOiJ1c2VyIn0.KOYnbURz-qco0uVLUESBcWWRJaOVYu3Ti0_IWaI-H4Q";

		loginPage.setUsername("not_richodemus");
		loginPage.setToken(token);

		assertThatThrownBy(() -> loginPage.toFeedPage().getAllFeeds()).isInstanceOf(AssertionError.class).hasMessageContaining("Expected status code <200> doesn't match actual status code <400>.");
	}

}
