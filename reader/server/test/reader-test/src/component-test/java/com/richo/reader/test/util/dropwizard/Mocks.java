package com.richo.reader.test.util.dropwizard;

import com.richo.reader.web.dropwizard.GuiceModule;
import com.richodemus.reader.common.google_cloud_storage_adapter.EventStore;
import com.richodemus.reader.common.google_cloud_storage_adapter.InMemoryEventStore;

class Mocks extends GuiceModule {
    @Override
    protected void bindEventStore() {
        bind(EventStore.class).to(InMemoryEventStore.class);
    }
}
