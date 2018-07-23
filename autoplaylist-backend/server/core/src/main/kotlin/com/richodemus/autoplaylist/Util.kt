package com.richodemus.autoplaylist

import io.github.vjames19.futures.jdk8.Future
import java.util.concurrent.CompletableFuture

fun <T> Iterable<CompletableFuture<T>>.flatten() = Future { this.map { it.join() } }
