package com.richo.reader.web.dropwizard.autoscanned;

import com.richo.reader.youtube_feed_service.PeriodicDownloadOrchestrator;
import io.dropwizard.lifecycle.Managed;

import javax.inject.Inject;

@SuppressWarnings("unused")
class DownloadManager implements Managed
{
	private final PeriodicDownloadOrchestrator orchestrator;

	@Inject
	DownloadManager(PeriodicDownloadOrchestrator orchestrator)
	{
		this.orchestrator = orchestrator;
	}

	@Override
	public void start() {
		orchestrator.start();
	}

	@Override
	public void stop() {
		orchestrator.stop();
	}
}
