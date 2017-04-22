package com.richo.reader.subscription_service

import com.richodemus.reader.events.Event
import io.reactivex.Observable

interface EventStore {
    fun add(event: Event)
    fun observe(): Observable<Event>
}
