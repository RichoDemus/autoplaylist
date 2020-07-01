package com.richodemus.reader.common.google_cloud_storage_adapter

internal interface Persistence {
    fun readEvents(): Sequence<Event>
    fun persist(event: Event)
}
