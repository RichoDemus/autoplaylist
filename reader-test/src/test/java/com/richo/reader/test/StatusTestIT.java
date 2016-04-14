package com.richo.reader.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.get;

public class StatusTestIT
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
	public void shouldReturn200OK() throws Exception
	{
		get("http://localhost:8080/").then().assertThat().statusCode(200);
	}
}
