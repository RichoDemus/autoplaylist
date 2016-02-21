package com.richo.reader.backend.persistence;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class PersistenceModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(ChannelPersister.class).annotatedWith(Names.named("InMemory")).to(InMemoryPersistence.class);
		//bind(ChannelPersister.class).annotatedWith(Names.named("FileSystem")).to(JsonFileSystemPersistence.class);
		//bind(ChannelPersister.class).annotatedWith(Names.named("YoutubeChannelPersister")).to(JsonFileSystemPersistence.class);
	}
}
