package com.richo.reader.web.dropwizard.autoscanned;

import com.richo.reader.youtube_feed_service.PeriodicDownloadOrchestrator;
import io.dropwizard.lifecycle.Managed;

import javax.inject.Inject;

class DownloadManager implements Managed
{
	private final PeriodicDownloadOrchestrator orchestrator;

	@Inject
	DownloadManager(PeriodicDownloadOrchestrator orchestrator)
	{
		this.orchestrator = orchestrator;
	}

	@Override
	public void start() throws Exception
	{
		orchestrator.start();
	}

	@Override
	public void stop() throws Exception
	{
		orchestrator.stop();
	}
}
