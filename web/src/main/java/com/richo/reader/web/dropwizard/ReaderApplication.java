package com.richo.reader.web.dropwizard;

import com.hubspot.dropwizard.guice.GuiceBundle;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReaderApplication extends Application<ReaderConfiguration>
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public static void main(String[] args) throws Exception
	{
		new ReaderApplication().run(args);
	}

	@Override
	public String getName()
	{
		return "Reader";
	}

	@Override
	public void initialize(Bootstrap<ReaderConfiguration> bootstrap)
	{
		bootstrap.addBundle(getGuiceBundle(bootstrap));
	}

	private GuiceBundle getGuiceBundle(Bootstrap<?> bootstrap)
	{
		final GuiceModule gcpWebModule = new GuiceModule();
		return GuiceBundle.<ReaderConfiguration>newBuilder().addModule(gcpWebModule)
				.enableAutoConfig("com.richo.reader.web.dropwizard.autoscanned")
				.setConfigClass(ReaderConfiguration.class)
				.build();
	}

	@Override
	public void run(ReaderConfiguration configuration,
					Environment environment)
	{
	}
}
