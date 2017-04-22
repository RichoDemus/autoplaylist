package com.richo.reader.web.dropwizard;

import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.google.inject.AbstractModule;
import com.richo.reader.web.dropwizard.autoscanned.MyBundle;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.GuiceBundle;

public class ReaderApplication extends Application<ReaderConfiguration>
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	protected AbstractModule module = new GuiceModule();

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
		bootstrap.getObjectMapper().registerModule(new KotlinModule());
		bootstrap.addBundle(new MyBundle());
		bootstrap.addBundle(GuiceBundle.builder()
				.modules(module)
				.enableAutoConfig("com.richo.reader.web.dropwizard.autoscanned")
				.build());
	}

	@Override
	public void run(ReaderConfiguration configuration,
					Environment environment)
	{
	}
}
