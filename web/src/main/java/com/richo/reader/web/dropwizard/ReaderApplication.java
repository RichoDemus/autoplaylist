package com.richo.reader.web.dropwizard;

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
		return "Casino Bots";
	}

	@Override
	public void initialize(Bootstrap<ReaderConfiguration> bootstrap)
	{
		bootstrap.addBundle(new MyBundle());
	}

	@Override
	public void run(ReaderConfiguration configuration,
					Environment environment)
	{
		// nothing to do yet

	}
}
