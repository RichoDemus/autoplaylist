package com.richo.reader.web.dropwizard.autoscanned.resource;

import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import java.io.IOException;
import java.util.Properties;

@Path("/info")
public class InfoResource
{
	@GET
	public String getInfo()
	{
		final Properties prop = new Properties();
		try
		{
			prop.load(getClass().getClassLoader().getResourceAsStream("resources.properties"));
			return prop.getProperty("git-sha-1");
		}
		catch (IOException e)
		{
			throw new InternalServerErrorException(e);
		}
	}
}
