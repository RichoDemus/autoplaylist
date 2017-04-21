package com.richo.reader.test.util.dropwizard;

import com.google.inject.AbstractModule;
import com.richo.reader.test.mocks.InMemoryEventStore;
import com.richodemus.reader.user_service.EventStore;

class Mocks extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(EventStore.class).to(InMemoryEventStore.class);
		bind(com.richo.reader.subscription_service.EventStore.class).to(InMemoryEventStore.class);
	}
}
