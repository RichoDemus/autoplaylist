package com.richo.reader.web.dropwizard.managed;

import com.richo.reader.youtube_feed_service.PeriodicDownloadOrchestrator;
import io.dropwizard.lifecycle.Managed;

import javax.inject.Inject;

public class DownloadManager implements Managed
{
	private final PeriodicDownloadOrchestrator orchestrator;

	@Inject
	public DownloadManager(PeriodicDownloadOrchestrator orchestrator)
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
