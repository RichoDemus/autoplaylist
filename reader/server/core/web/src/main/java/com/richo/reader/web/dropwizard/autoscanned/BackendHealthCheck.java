package com.richo.reader.web.dropwizard.autoscanned;

import com.richo.reader.backend.Backend;
import com.richo.reader.backend.model.FeedWithoutItems;
import com.richodemus.reader.dto.Username;
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
		return "Test find feeds";
	}

	@Override
	protected Result check() {
		final List<FeedWithoutItems> feeds;
		try
		{
			feeds = backend.getAllFeedsWithoutItems(new Username("RichoDemus"));
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
