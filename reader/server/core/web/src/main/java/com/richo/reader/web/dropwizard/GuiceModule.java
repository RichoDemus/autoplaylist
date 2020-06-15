package com.richo.reader.web.dropwizard;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.richo.reader.backend.inject.BackendModule;
import com.richo.reader.web.authentication.UserServiceBridge;
import com.richo.reader.web.dropwizard.autoscanned.BackendHealthCheck;
import com.richodemus.dropwizard.jwt.UserService;
import com.richodemus.reader.common.google_cloud_storage_adapter.EventStore;
import com.richodemus.reader.common.google_cloud_storage_adapter.GoogleCloudStorageAdapter;
import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Duration;
import java.util.Optional;

import static com.google.inject.name.Names.named;

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
		bindApiKey();
	}

	protected void bindApiKey() {
		bind(String.class).annotatedWith(named("apiKey")).toInstance(getApiKey());
	}

	protected void bindEventStore()
	{
		bind(EventStore.class).to(GoogleCloudStorageAdapter.class);
	}

	private String getApiKey() {
		return Optional.ofNullable(System.getProperty("reader.gcs.key"))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.orElseThrow(() -> new IllegalArgumentException("Missing property reader.gcs.key/$GCS_KEY"));
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
