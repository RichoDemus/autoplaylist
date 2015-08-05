package com.richo.reader.web.dropwizard;

import com.google.inject.AbstractModule;
import com.richo.reader.backend.inject.BackendModule;

public class GuiceModule extends AbstractModule
{
	private final boolean offlineMode;

	public GuiceModule(final boolean offlineMode)
	{
		this.offlineMode = offlineMode;
	}

	@Override
	public void configure()
	{
		install(new BackendModule(offlineMode));
	}
}