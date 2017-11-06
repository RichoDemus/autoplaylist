package com.richo.reader.test.util.dropwizard;

import com.richo.reader.web.dropwizard.GuiceModule;
import com.richodemus.reader.common.kafka_adapter.EventStore;

class Mocks extends GuiceModule {
    @Override
    protected void bindEventStore() {
        bind(EventStore.class).to(com.richodemus.reader.common.kafka_adapter.InMemoryEventStore.class);
    }
}
