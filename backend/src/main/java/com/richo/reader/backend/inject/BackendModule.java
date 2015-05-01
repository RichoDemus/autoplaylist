package com.richo.reader.backend.inject;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.richo.reader.backend.Backend;
import com.richo.reader.backend.user.SimpleHashmapUserStorage;
import com.richo.reader.backend.user.UserStorage;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class BackendModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(Backend.class);
		bind(UserStorage.class).to(SimpleHashmapUserStorage.class);
		bind(Duration.class).toInstance(Duration.of(1, ChronoUnit.HOURS));
		bind(String.class).annotatedWith(Names.named("apiKey")).toInstance("AIzaSyChI7lMyLfc1ckOqcC-z2Oz-Lrq6d09x30");
	}
}
