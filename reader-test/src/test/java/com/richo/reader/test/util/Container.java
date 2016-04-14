package com.richo.reader.test.util;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.fail;

class Container implements AutoCloseable
{
	private final DefaultDockerClient docker;
	private final String id;

	Container(String image, final Set<String> ports) throws Exception
	{
		if (!image.contains(":"))
		{
			image = image + ":latest";
		}
		docker = DefaultDockerClient.fromEnv().build();

		final Map<String, List<PortBinding>> portBindings = new HashMap<>();
		for (String port : ports)
		{
			List<PortBinding> hostPorts = new ArrayList<PortBinding>();
			hostPorts.add(PortBinding.of("0.0.0.0", port));
			portBindings.put(port, hostPorts);
		}

		final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

		// Create container with exposed ports
		final ContainerConfig containerConfig = ContainerConfig.builder()
				.env("YOUTUBE_URL=http://youtube-mock")
				.hostConfig(hostConfig)
				.image(image).exposedPorts(ports)
				.build();

		final ContainerCreation creation = docker.createContainer(containerConfig);
		id = creation.id();

		System.out.println("container id: " + id);

		docker.startContainer(id);
	}

	void awaitStartup(final BooleanSupplier supplier) throws Exception
	{
		testIfStartedUp(300, supplier);
	}

	private void testIfStartedUp(final int retriesLeft, final BooleanSupplier supplier) throws Exception
	{
		if (retriesLeft == 0)
		{
			fail("Container never started");
		}
		if (!isRunning(supplier))
		{
			Thread.sleep(10L);
			testIfStartedUp(retriesLeft - 1, supplier);
		}
	}

	private boolean isRunning(BooleanSupplier supplier)
	{
		try
		{
			return supplier.getAsBoolean();
		}
		catch (Exception e)
		{
			return false;
		}
	}

	@Override
	public void close() throws Exception
	{
		docker.stopContainer(id, 1);
		docker.removeContainer(id);
		docker.close();
	}
}
