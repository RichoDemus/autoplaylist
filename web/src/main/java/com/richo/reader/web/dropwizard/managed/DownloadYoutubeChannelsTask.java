package com.richo.reader.web.dropwizard.managed;

import com.google.common.collect.ImmutableMultimap;
import com.richo.reader.youtube_feed_service.PeriodicDownloadOrchestrator;
import io.dropwizard.servlets.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.PrintWriter;

public class DownloadYoutubeChannelsTask extends Task
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final PeriodicDownloadOrchestrator orchestrator;

	@Inject
	public DownloadYoutubeChannelsTask(PeriodicDownloadOrchestrator orchestrator)
	{
		super("download");
		this.orchestrator = orchestrator;
	}

	@Override
	public void execute(ImmutableMultimap<String, String> immutableMultimap, PrintWriter printWriter) throws Exception
	{
		logger.info("Execute...");
		orchestrator.downloadEverythingOnce();
	}
}
