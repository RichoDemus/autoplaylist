package com.richo.reader.test;

import com.spotify.docker.client.DefaultDockerClient;
import org.junit.Test;

import static org.junit.Assert.fail;

public class StatusTestIT
{
	@Test
	public void failMeIT() throws Exception
	{
		DefaultDockerClient.fromEnv().build();
		fail("Autofail");

	}
}