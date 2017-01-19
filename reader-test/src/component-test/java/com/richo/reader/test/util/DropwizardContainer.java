package com.richo.reader.test.util;

import com.google.common.collect.Sets;

import java.util.HashSet;

import static com.jayway.restassured.RestAssured.get;

public class DropwizardContainer implements AutoCloseable, TestableApplication
{
	private static final String PORT = "8080";
	private static final String ADMIN_PORT = "8081";

	private final Container container;

	public DropwizardContainer(final String image)
	{
		this(image, Sets.newHashSet("YOUTUBE_URL=http://localhost:80/"));
	}

	public DropwizardContainer(String image, HashSet<String> env)
	{
		container = new Container(image, env);
		container.awaitStartup(() -> ping() == 200);
	}

	private int ping()
	{
		return get("http://localhost:" + getAdminPort() + "/ping").then().extract().statusCode();
	}

	@Override
	public void close()
	{
		container.close();
	}

	@Override
	public int getHttpPort()
	{
		return container.getExternalPort(PORT)
				.orElseThrow(() -> new IllegalStateException("Http port is not exposed"));
	}

	@Override
	public int getAdminPort()
	{
		return container.getExternalPort(ADMIN_PORT)
				.orElseThrow(() -> new IllegalStateException("Admin port is not exposed"));
	}

	@Override
	public String getIp()
	{
		return container.getIp();
	}

	public String getLogs()
	{
		return container.getLogs();
	}
}
