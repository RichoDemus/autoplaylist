package com.richo.reader.backend.inject;

import com.google.inject.AbstractModule;
import com.richo.reader.backend.Backend;

public class BackendModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(Backend.class);
	}
}
