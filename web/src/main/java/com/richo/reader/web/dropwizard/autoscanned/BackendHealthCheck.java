package com.richo.reader.web.dropwizard.autoscanned;

import com.richo.reader.backend.Backend;
import com.richo.reader.backend.model.Feed;
import com.richodemus.reader.dto.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.installer.feature.health.NamedHealthCheck;

import javax.inject.Inject;
import java.util.List;

public class BackendHealthCheck extends NamedHealthCheck
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Backend backend;

	@Inject
	BackendHealthCheck(Backend backend)
	{
		this.backend = backend;
	}

	@Override
	public String getName()
	{
		return "Test get feeds";
	}

	@Override
	protected Result check() throws Exception
	{
		final List<Feed> feeds;
		try
		{
			feeds = backend.getAllFeedsWithoutItems(new UserId("RichoDemus"));
		}
		catch (Exception e)
		{
			logger.error("Healthcheck failed ", e);
			return Result.unhealthy("Exception when getting feeds: " + e.getMessage());
		}

		if (feeds == null)
		{
			return Result.unhealthy("Got null feeds");
		}

		if (feeds.size() == 0)
		{
			return Result.unhealthy("Got no feeds");
		}

		return Result.healthy();
	}
}
