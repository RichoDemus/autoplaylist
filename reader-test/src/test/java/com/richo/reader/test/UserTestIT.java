package com.richo.reader.test;

import com.jayway.restassured.RestAssured;
import com.richo.reader.test.util.DropwizardContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTestIT
{
	private DropwizardContainer container;

	@Before
	public void setUp() throws Exception
	{
		container = new DropwizardContainer("richodemus/reader");
	}

	@After
	public void tearDown() throws Exception
	{
		container.close();
	}

	@Test
	public void shouldNotBeAbleToLoginIfUserDoesntExist() throws Exception
	{
		RestAssured
				.given().body("MyPassword")
				.when().post("http://localhost:8080/api/users/richodemus/sessions")
				.then().assertThat().statusCode(400);
	}

	@Test
	public void shouldCreateUser() throws Exception
	{
		RestAssured
				.given().body("richodemus")
				.when().post("http://localhost:8080/api/users")
				.then().assertThat().statusCode(200);
	}

	@Test
	public void shouldLoginUser() throws Exception
	{
		final String username = "richodemus";
		RestAssured
				.given().body(username)
				.when().post("http://localhost:8080/api/users")
				.then().assertThat().statusCode(200);

		final String result = RestAssured
				.given().body("123456789qwertyuio123qweasd")
				.when().post("http://localhost:8080/api/users/" + username + "/sessions")
				.then().assertThat().statusCode(200).extract().body().jsonPath().get("username");

		assertThat(result).isEqualTo(username);
	}

	@Test
	public void shouldNotLoginUserWithInvalidPassword() throws Exception
	{
		RestAssured
				.given().body("richodemus")
				.when().post("http://localhost:8080/api/users")
				.then().assertThat().statusCode(200);

		RestAssured
				.given().body("not_the_right_password")
				.when().post("http://localhost:8080/api/users/richodemus/sessions")
				.then().assertThat().statusCode(400);
	}
}
