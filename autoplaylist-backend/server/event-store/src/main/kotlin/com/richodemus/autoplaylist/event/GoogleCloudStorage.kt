package com.richodemus.autoplaylist.event

import java.util.concurrent.CompletableFuture

interface GoogleCloudStorage {
    fun read(): CompletableFuture<List<Pair<Long, CompletableFuture<ByteArray>>>>
    fun write(filename: String, data: ByteArray)
}
