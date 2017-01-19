package com.richo.reader.web.dropwizard;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.richo.reader.backend.inject.BackendModule;
import com.richo.reader.web.authentication.UserServiceBridge;
import com.richo.reader.web.dropwizard.autoscanned.BackendHealthCheck;
import com.richodemus.dropwizard.jwt.UserService;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Duration;

class GuiceModule extends AbstractModule
{
	@Override
	public void configure()
	{
		install(new BackendModule());
		bind(UserService.class).to(UserServiceBridge.class);
		bind(Duration.class).annotatedWith(Names.named("tokenDuration")).toInstance(Duration.ofDays(30L));
		bind(String.class).annotatedWith(Names.named("secret")).toInstance("qwel12319zxc90we23rnlsdfpsdf09sdfk323rlksdfsd");
		bind(BackendHealthCheck.class);
	}

	//todo move to backend guice stuff
	@Provides
	@Named("saveRoot")
	@Inject
	public String provideSaveRoot(ReaderConfiguration configuration)
	{
		return configuration.getSaveRoot();
	}
}
