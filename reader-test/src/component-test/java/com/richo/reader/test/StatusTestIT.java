package com.richo.reader.test;

import com.richo.reader.test.util.TestableApplication;
import com.richo.reader.test.util.DropwizardContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.get;

public class StatusTestIT
{
	private TestableApplication target;

	@Before
	public void setUp() throws Exception
	{
		target = new DropwizardContainer("richodemus/reader");
	}

	@After
	public void tearDown() throws Exception
	{
		target.close();
	}

	@Test
	public void shouldReturn200OK() throws Exception
	{
		get("http://localhost:" + target.getAdminPort() + "/").then().assertThat().statusCode(200);
	}
}
