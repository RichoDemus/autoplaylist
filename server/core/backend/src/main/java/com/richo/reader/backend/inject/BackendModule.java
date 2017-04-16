package com.richo.reader.backend.inject;

import com.google.inject.AbstractModule;
import com.richo.reader.backend.Backend;
import com.richo.reader.backend.user.UserRepository;
import com.richo.reader.backend.user.UserServicePort;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static com.google.inject.name.Names.named;

public class BackendModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(Backend.class);
		bind(Duration.class).toInstance(Duration.of(1, ChronoUnit.DAYS));
		bind(String.class).annotatedWith(named("apiKey")).toInstance("AIzaSyChI7lMyLfc1ckOqcC-z2Oz-Lrq6d09x30");
		bind(UserRepository.class).to(UserServicePort.class);
	}
}
