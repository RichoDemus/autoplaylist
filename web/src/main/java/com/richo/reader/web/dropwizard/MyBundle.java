package com.richo.reader.web.dropwizard;

import com.hubspot.dropwizard.guice.GuiceBundle;
import com.richo.reader.web.resources.SessionResource;
import com.richo.reader.web.resources.FeedResource;
import com.richo.reader.web.resources.UserResource;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.java8.Java8Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyBundle implements ConfiguredBundle<ReaderConfiguration>
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void initialize(Bootstrap<?> bootstrap)
	{
		bootstrap.addBundle(getGuiceBundle(bootstrap));
		bootstrap.addBundle(new AssetsBundle("/webroot/", "/", "index.html", "static"));
		bootstrap.addBundle(new Java8Bundle());
	}

	@Override
	public void run(ReaderConfiguration configuration, Environment environment) throws Exception
	{
		logger.info("Registering Testresouce");
		environment.jersey().register(FeedResource.class);
		environment.jersey().register(SessionResource.class);
		environment.jersey().register(UserResource.class);
	}

	private GuiceBundle getGuiceBundle(Bootstrap<?> bootstrap)
	{
		/*
		TODO do this in a nicer way, prefferably from the config-file
		but I don't know how to get access to the configuration object from initialize
		 */
		final GuiceModule gcpWebModule = new GuiceModule(true);
		return GuiceBundle.<ReaderConfiguration>newBuilder().addModule(gcpWebModule)
				.setConfigClass(ReaderConfiguration.class)
				.build();
	}
}
