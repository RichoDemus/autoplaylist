package com.richo.reader.test;

import com.richo.reader.test.util.TestableApplication;
import com.richo.reader.test.util.TestableApplicationProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.get;

public class StatusTestIT
{
	private TestableApplication target;

	@Before
	public void setUp() {
		target = new TestableApplicationProvider().readerApplication();
	}

	@After
	public void tearDown() {
		target.close();
	}

	@Test
	public void shouldReturn200OK() {
		get("http://localhost:" + target.getAdminPort() + "/").then().assertThat().statusCode(200);
	}
}
