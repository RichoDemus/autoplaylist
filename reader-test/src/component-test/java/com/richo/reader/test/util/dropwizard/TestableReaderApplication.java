package com.richo.reader.test.util.dropwizard;

import com.richo.reader.test.util.TestableApplication;
import com.richo.reader.web.dropwizard.ReaderApplication;
import com.richo.reader.web.dropwizard.ReaderConfiguration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;

import java.util.UUID;

public class TestableReaderApplication implements TestableApplication
{
	private DropwizardTestSupport<ReaderConfiguration> support;

	public TestableReaderApplication(final int youtubeMockPort)
	{
		if (youtubeMockPort != -1)
		{
			System.setProperty("YOUTUBE_URL", "http://localhost:" + youtubeMockPort + "/");
		}
		support = new DropwizardTestSupport<>(ReaderApplication.class,
				"../docker/config.yaml",
				ConfigOverride.config("saveRoot", "build/saveRoots/" + UUID.randomUUID()),
				ConfigOverride.config("server.applicationConnectors[0].port", "0"),
				ConfigOverride.config("server.adminConnectors[0].port", "0"));
		support.before();
	}

	public TestableReaderApplication()
	{
		this(-1);
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
		System.clearProperty("YOUTUBE_URL");
	}
}
