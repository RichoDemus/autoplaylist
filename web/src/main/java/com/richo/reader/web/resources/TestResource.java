package com.richo.reader.web.resources;

import com.richo.casinobots.api.Bot;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Set;

@Path("/test")
public class TestResource
{
	private final InjectMe injectable;
	private final Set<Bot> allBots;

	@Inject
	public TestResource(InjectMe injectable, Set<Bot> allBots)
	{
		this.injectable = injectable;
		this.allBots = allBots;
	}

	@GET
	public String get()
	{
		return injectable.getString() + " there are " + allBots.size() + " bots";
	}
}
