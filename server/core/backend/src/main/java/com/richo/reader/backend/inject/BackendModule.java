package com.richo.reader.backend.inject;

import com.google.inject.AbstractModule;
import com.richo.reader.backend.Backend;
import com.richo.reader.backend.feed.FeedRepository;
import com.richo.reader.backend.feed.FeedServicePort;
import com.richo.reader.backend.subscription.SubscriptionRepository;
import com.richo.reader.backend.subscription.SubscriptionServicePort;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static com.google.inject.name.Names.named;

public class BackendModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(Backend.class);
		bind(Duration.class).toInstance(Duration.of(1, ChronoUnit.DAYS));
		bind(String.class).annotatedWith(named("apiKey")).toInstance("AIzaSyChI7lMyLfc1ckOqcC-z2Oz-Lrq6d09x30");
		bind(SubscriptionRepository.class).to(SubscriptionServicePort.class);
		bind(FeedRepository.class).to(FeedServicePort.class);
	}
}
