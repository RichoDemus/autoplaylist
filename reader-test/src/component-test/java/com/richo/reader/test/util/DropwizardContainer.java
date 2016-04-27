package com.richo.reader.test.util;

import com.google.common.collect.Sets;

import java.util.HashSet;

import static com.jayway.restassured.RestAssured.get;

public class DropwizardContainer implements AutoCloseable
{
	private static final String PORT = "8080";
	private static final String ADMIN_PORT = "8081";

	private final Container container;

	public DropwizardContainer(final String image) throws Exception
	{
		this(image, Sets.newHashSet(Sets.newHashSet("YOUTUBE_URL=http://localhost:80/")));
	}

	private DropwizardContainer(String image, HashSet<String> env) throws Exception
	{
		container = new Container(image, env);
		container.awaitStartup(() -> get("http://localhost:" + getAdminPort() + "/ping").then().extract().statusCode() == 200);
	}

	@Override
	public void close() throws Exception
	{
		container.close();
	}

	public int getHttpPort()
	{
		return container.getExternalPort(PORT)
				.orElseThrow(() -> new RuntimeException("Http port is not exposed"));
	}

	public int getAdminPort()
	{
		return container.getExternalPort(ADMIN_PORT)
				.orElseThrow(() -> new RuntimeException("Admin port is not exposed"));
	}
}
