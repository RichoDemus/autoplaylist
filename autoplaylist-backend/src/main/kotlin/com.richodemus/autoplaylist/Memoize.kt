package com.richodemus.autoplaylist

class Memoize<in T, out R>(private val f: (T) -> R) : (T) -> R {
    private val values = mutableMapOf<T, R>()
    override fun invoke(x: T) = values.getOrPut(x) { f(x) }
}

fun <T, R> ((T) -> R).memoize(): (T) -> R = Memoize(this)
