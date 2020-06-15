package com.richo.reader.test.util.dropwizard;

import com.richo.reader.web.dropwizard.GuiceModule;
import com.richodemus.reader.common.google_cloud_storage_adapter.EventStore;
import com.richodemus.reader.common.google_cloud_storage_adapter.InMemoryEventStore;

import static com.google.inject.name.Names.named;

class Mocks extends GuiceModule {
    @Override
    protected void bindEventStore() {
        bind(EventStore.class).to(InMemoryEventStore.class);
    }

    @Override
    protected void bindApiKey() {
        bind(String.class).annotatedWith(named("apiKey")).toInstance("");
    }
}
