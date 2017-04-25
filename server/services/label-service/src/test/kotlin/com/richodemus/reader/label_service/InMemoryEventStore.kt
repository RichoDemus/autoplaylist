package com.richodemus.reader.label_service

import com.richodemus.reader.events.Event
import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject

class InMemoryEventStore : EventStore {
    private val replaySubject = ReplaySubject.create<Event>()

    override fun add(event: Event) = replaySubject.onNext(event)

    override fun observe(): Observable<Event> = replaySubject
}
