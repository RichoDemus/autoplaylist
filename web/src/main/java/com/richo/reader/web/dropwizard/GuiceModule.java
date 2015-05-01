package com.richo.reader.web.dropwizard;

import com.google.inject.AbstractModule;
import com.richo.reader.backend.inject.BackendModule;

public class GuiceModule extends AbstractModule
{

	@Override
	public void configure()
	{
		install(new BackendModule());
	}
}