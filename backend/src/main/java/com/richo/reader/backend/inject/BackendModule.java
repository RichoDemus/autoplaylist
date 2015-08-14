package com.richo.reader.backend.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.richo.reader.backend.Backend;
import com.richo.reader.backend.persistence.ChannelPersister;
import com.richo.reader.backend.persistence.InMemoryPersistence;
import com.richo.reader.backend.persistence.JsonFileSystemPersistence;
import com.richo.reader.backend.persistence.YoutubeChannelPersistence;
import com.richo.reader.backend.user.InMemoryUserPersistence;
import com.richo.reader.backend.user.JsonFileSystemUserPersistence;
import com.richo.reader.backend.user.UserPersister;
import com.richo.reader.backend.youtube.OfflineChannelService;
import com.richo.reader.backend.youtube.YoutubeChannelService;
import com.richo.reader.backend.youtube.download.YoutubeChannelDownloader;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class BackendModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(Backend.class);
		bind(Duration.class).toInstance(Duration.of(1, ChronoUnit.HOURS));
		bind(String.class).annotatedWith(Names.named("apiKey")).toInstance("AIzaSyChI7lMyLfc1ckOqcC-z2Oz-Lrq6d09x30");
		bind(String.class).annotatedWith(Names.named("saveRoot")).toInstance("data/");

		bind(ChannelPersister.class).annotatedWith(Names.named("InMemory")).to(InMemoryPersistence.class);
		bind(ChannelPersister.class).annotatedWith(Names.named("FileSystem")).to(JsonFileSystemPersistence.class);

		bind(UserPersister.class).annotatedWith(Names.named("InMemory")).to(InMemoryUserPersistence.class);
		bind(UserPersister.class).annotatedWith(Names.named("FileSystem")).to(JsonFileSystemUserPersistence.class);
	}

	@Inject
	@Provides
	public YoutubeChannelService provideYoutubeChannelService(
			@Named("offlineMode") Boolean offlineMode,
			YoutubeChannelPersistence cache,
			YoutubeChannelDownloader downloader,
			Duration duration)
	{
		if (offlineMode)
		{
			return new OfflineChannelService(cache);
		}
		return new YoutubeChannelService(downloader, cache, duration);
	}
}
