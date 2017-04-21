package com.richo.reader.test.mocks;

import com.richodemus.reader.events.Event;
import com.richodemus.reader.user_service.EventStore;
import io.reactivex.Observable;
import io.reactivex.subjects.ReplaySubject;

import javax.inject.Singleton;

@Singleton
public class InMemoryEventStore implements EventStore, com.richo.reader.subscription_service.EventStore
{
	private ReplaySubject<Event> replaySubject = ReplaySubject.create();

	@Override
	public void add(final Event event)
	{
		replaySubject.onNext(event);
	}

	@Override
	public Observable<Event> observe()
	{
		return replaySubject;
	}
}
