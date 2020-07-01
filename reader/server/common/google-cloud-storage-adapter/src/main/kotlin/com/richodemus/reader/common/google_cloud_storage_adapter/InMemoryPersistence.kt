package com.richodemus.reader.common.google_cloud_storage_adapter

import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Primary // so that it's used in tests and dev properly..
@Profile(value = ["dev", "test"])
@Component
internal class InMemoryPersistence : Persistence {
    private val events = mutableListOf<Event>()
    override fun readEvents(): Sequence<Event> {
        return events.asSequence()
    }

    override fun persist(event: Event) {
        events.add(event)
    }
}