package com.richo.reader.test.util.dropwizard;

import com.richo.reader.test.util.TestableApplication;
import com.richodemus.reader.mock.youtube.YoutubemockApplication;
import com.richodemus.reader.mock.youtube.YoutubemockConfiguration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;

public class YoutubeMock implements TestableApplication
{
	private DropwizardTestSupport<YoutubemockConfiguration> support;

	public YoutubeMock()
	{
		support = new DropwizardTestSupport<>(YoutubemockApplication.class,
				"src/component-test/resources/youtublemockconfig.yaml",
				ConfigOverride.config("server.applicationConnectors[0].port", "0"),
				ConfigOverride.config("server.adminConnectors[0].port", "0"));
		support.before();
	}

	@Override
	public String getIp()
	{
		return "localhost";
	}

	@Override
	public int getHttpPort()
	{
		return support.getLocalPort();
	}

	@Override
	public int getAdminPort()
	{
		return support.getAdminPort();
	}

	@Override
	public void close()
	{
		support.after();
	}
}
