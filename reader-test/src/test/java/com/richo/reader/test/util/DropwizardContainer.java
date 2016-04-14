package com.richo.reader.test.util;

import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.function.BooleanSupplier;

import static com.jayway.restassured.RestAssured.get;

public class DropwizardContainer implements AutoCloseable
{
	private static final String PORT = "8080";
	private static final String ADMIN_PORT = "8081";
	private static final HashSet<String> PORTS = Sets.newHashSet(PORT, ADMIN_PORT);
	private static final BooleanSupplier DROPWIZARD_CHECK = () -> get("http://localhost:8081/ping").then().extract().statusCode() == 200;

	private final Container container;

	public DropwizardContainer(final String image) throws Exception
	{
		container = new Container(image, PORTS);
		container.awaitStartup(DropwizardContainer.DROPWIZARD_CHECK);
	}

	@Override
	public void close() throws Exception
	{
		container.close();
	}
}
