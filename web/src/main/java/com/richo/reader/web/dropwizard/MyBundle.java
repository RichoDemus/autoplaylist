package com.richo.reader.web.dropwizard;

import com.hubspot.dropwizard.guice.GuiceBundle;
import com.richo.reader.web.resources.FeedResource;
import com.richo.reader.web.resources.LabelResource;
import com.richo.reader.web.resources.SessionResource;
import com.richo.reader.web.resources.UserResource;
import com.richodemus.dropwizard.jwt.AuthenticationRequestFilter;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.java8.Java8Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
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
		environment.jersey().register(LabelResource.class);


		//Setup dropwizard-jwt
		environment.jersey().register(AuthenticationRequestFilter.class);
		environment.jersey().register(RolesAllowedDynamicFeature.class);
	}

	private GuiceBundle getGuiceBundle(Bootstrap<?> bootstrap)
	{
		final GuiceModule gcpWebModule = new GuiceModule();
		return GuiceBundle.<ReaderConfiguration>newBuilder().addModule(gcpWebModule)
				.setConfigClass(ReaderConfiguration.class)
				.build();
	}
}
