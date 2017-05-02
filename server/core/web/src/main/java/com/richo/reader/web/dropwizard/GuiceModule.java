package com.richo.reader.web.dropwizard;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.richo.reader.backend.inject.BackendModule;
import com.richo.reader.web.authentication.UserServiceBridge;
import com.richo.reader.web.dropwizard.autoscanned.BackendHealthCheck;
import com.richodemus.dropwizard.jwt.UserService;
import com.richodemus.reader.common.chronicler_adapter.ChroniclerAdapter;
import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Duration;

public class GuiceModule extends DropwizardAwareModule<ReaderConfiguration>
{
	@Override
	public void configure()
	{
		install(new BackendModule());
		bind(UserService.class).to(UserServiceBridge.class);
		bind(Duration.class).annotatedWith(Names.named("tokenDuration")).toInstance(Duration.ofDays(30L));
		bind(String.class).annotatedWith(Names.named("secret")).toInstance("qwel12319zxc90we23rnlsdfpsdf09sdfk323rlksdfsd");
		bind(BackendHealthCheck.class);
		bindEventStore();
		bind(MetricRegistry.class).toInstance(environment().metrics());
	}

	protected void bindEventStore()
	{
		bind(com.richodemus.reader.label_service.EventStore.class).to(ChroniclerAdapter.class);
		bind(com.richo.reader.subscription_service.EventStore.class).to(ChroniclerAdapter.class);
		bind(com.richodemus.reader.user_service.EventStore.class).to(ChroniclerAdapter.class);
	}

	//todo move to backend guice stuff
	@Provides
	@Named("saveRoot")
	@Inject
	public String provideSaveRoot(ReaderConfiguration configuration)
	{
		return configuration.getSaveRoot();
	}
}
