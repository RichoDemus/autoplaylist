package com.richo.reader.test.util.dropwizard;

import com.richo.reader.test.mocks.InMemoryEventStore;
import com.richo.reader.web.dropwizard.GuiceModule;

class Mocks extends GuiceModule
{
	@Override
	protected void bindEventStore()
	{
		bind(com.richodemus.reader.user_service.EventStore.class).to(InMemoryEventStore.class);
		bind(com.richo.reader.subscription_service.EventStore.class).to(InMemoryEventStore.class);
	}
}
