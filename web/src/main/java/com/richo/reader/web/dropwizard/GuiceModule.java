package com.richo.reader.web.dropwizard;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.richo.reader.backend.inject.BackendModule;
import com.richo.reader.web.authentication.UserServiceBridge;
import com.richo.reader.web.dropwizard.autoscanned.BackendHealthCheck;
import com.richodemus.dropwizard.jwt.UserService;

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
}
