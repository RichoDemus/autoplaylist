package com.richo.reader.backend.inject;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.richo.reader.backend.Backend;
import com.richo.reader.backend.user.InMemoryUserPersistence;
import com.richo.reader.backend.user.JsonFileSystemUserPersistence;
import com.richo.reader.backend.user.UserPersister;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class BackendModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(Backend.class);
		bind(Duration.class).toInstance(Duration.of(1, ChronoUnit.DAYS));
		bind(String.class).annotatedWith(Names.named("apiKey")).toInstance("AIzaSyChI7lMyLfc1ckOqcC-z2Oz-Lrq6d09x30");
		bind(String.class).annotatedWith(Names.named("saveRoot")).toInstance("data/");

		//bind(ChannelPersister.class).annotatedWith(Names.named("FileSystem")).to(JsonFileSystemPersistence.class);

		bind(UserPersister.class).annotatedWith(Names.named("InMemory")).to(InMemoryUserPersistence.class);
		bind(UserPersister.class).annotatedWith(Names.named("FileSystem")).to(JsonFileSystemUserPersistence.class);
	}

}
