package com.richo.reader.test;

import com.jayway.restassured.RestAssured;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

		RestAssured
				.given().body("123456789qwertyuio123qweasd")
				.when().post("http://localhost:8080/api/users/richodemus/sessions")
				.then().assertThat().statusCode(200);

	}
}
