package com.richodemus.autoplaylist.event.gcs

import kotlinx.coroutines.Deferred

interface GoogleCloudStorage {
    fun read(): List<Pair<Long, Deferred<ByteArray>>>
    fun write(filename: String, data: ByteArray)
}
