package com.richo.reader.web.dropwizard.autoscanned.resource;

import com.richo.reader.web.dto.DownloadJobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("user") //todo admin users etc
public class AdminResource
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@GET
	@Path("/download")
	public DownloadJobStatus getStatus() {
		return new DownloadJobStatus(LocalDateTime.now().minusDays(1), false);
	}

	@POST
	@Path("/download")
	public void runDownloadJob() {
		logger.info("User triggered download job");
	}
}
