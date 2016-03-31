package com.richo.reader.test;

import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;

import static com.jayway.restassured.RestAssured.get;

public class StatusTestIT
{
	private static final String PORT = "8080";
	private static final String ADMIN_PORT = "8081";
	private static final String PING = "http://localhost:" + ADMIN_PORT + "/ping";
	private static final HashSet<String> PORTS = Sets.newHashSet(PORT, ADMIN_PORT);

	private Container container;

	@Before
	public void setUp() throws Exception
	{
		container = new Container("richodemus/reader", PORTS);
		container.awaitStartup(this::testIfContainerStarted);
	}

	private boolean testIfContainerStarted()
	{
		return get(PING).then().extract().statusCode() == 200;
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
