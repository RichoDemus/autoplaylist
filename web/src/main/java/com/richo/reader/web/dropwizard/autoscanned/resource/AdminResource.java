package com.richo.reader.web.dropwizard.autoscanned.resource;

import com.richo.reader.web.dto.DownloadJobStatus;
import com.richo.reader.youtube_feed_service.PeriodicDownloadOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user") //todo admin users etc
public class AdminResource
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final PeriodicDownloadOrchestrator periodicDownloadOrchestrator;

	@Inject
	public AdminResource(final PeriodicDownloadOrchestrator periodicDownloadOrchestrator)
	{
		this.periodicDownloadOrchestrator = periodicDownloadOrchestrator;
	}

	@GET
	@Path("/download")
	public DownloadJobStatus getStatus() {
		return new DownloadJobStatus(periodicDownloadOrchestrator.lastRun(), periodicDownloadOrchestrator.isRunning());
	}

	@POST
	@Path("/download")
	public void runDownloadJob() {
		logger.info("User triggered download job");
		periodicDownloadOrchestrator.downloadEverythingOnce();
	}
}
