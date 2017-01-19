package com.richo.reader.test.util.dropwizard;

import com.richo.reader.test.util.TestableApplication;
import com.richodemus.reader.mock.youtube.YoutubemockApplication;
import com.richodemus.reader.mock.youtube.YoutubemockConfiguration;
import io.dropwizard.testing.DropwizardTestSupport;

public class YoutubeMock implements TestableApplication
{
	private DropwizardTestSupport<YoutubemockConfiguration> support;

	public YoutubeMock()
	{
		support = new DropwizardTestSupport<>(YoutubemockApplication.class,
				"src/component-test/resources/youtublemockconfig.yaml");
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
