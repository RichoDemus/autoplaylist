package com.richodemus.autoplaylist.event

interface EventStore {
    fun consume(messageListener: (Event) -> Unit)
    fun produce(event: Event)
    /**
     * Add a temporary message listener
     *
     * The listener will retrieve events until it returns true
     * It's typically used to complete futures
     */
    fun addTemporaryListener(messageListener: (Event) -> Boolean)
}
