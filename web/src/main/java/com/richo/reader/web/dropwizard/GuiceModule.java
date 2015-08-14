package com.richo.reader.web.dropwizard;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.richo.reader.backend.inject.BackendModule;

import javax.inject.Inject;
import javax.inject.Named;

public class GuiceModule extends AbstractModule
{
	@Override
	public void configure()
	{
		install(new BackendModule());
	}

	@Inject
	@Provides
	@Named("offlineMode")
	public Boolean provideOfflineMode(ReaderConfiguration configuration)
	{
		return configuration.isOfflineMode();
	}
}