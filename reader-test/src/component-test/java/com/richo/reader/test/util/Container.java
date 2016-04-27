package com.richo.reader.test.util;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.NetworkSettings;
import com.spotify.docker.client.messages.PortBinding;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.fail;

class Container implements AutoCloseable
{
	private final DefaultDockerClient docker;
	private final String id;

	Container(String image, Set<String> env) throws Exception
	{
		if (!image.contains(":"))
		{
			image = image + ":latest";
		}
		docker = DefaultDockerClient.fromEnv().build();

		final HostConfig hostConfig = HostConfig.builder().publishAllPorts(true).build();

		final ContainerConfig.Builder builder = ContainerConfig.builder();
		env.forEach(builder::env);
		final ContainerConfig containerConfig = builder
				.hostConfig(hostConfig)
				.image(image)
				.build();

		final ContainerCreation creation = docker.createContainer(containerConfig);
		id = creation.id();

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

	Optional<Integer> getExternalPort(String internalPort)
	{
		final String port = ensureProtocol(internalPort);

		return Stream.of(id)
				.map(this::getContainerInfo)
				.map(ContainerInfo::networkSettings)
				.map(NetworkSettings::ports)
				.map(Map::entrySet)
				.flatMap(Collection::stream)
				.filter(p -> p.getKey().equals(port))
				.map(Map.Entry::getValue)
				.flatMap(Collection::stream)
				.map(PortBinding::hostPort)
				.map(Integer::parseInt)
				.findAny();
	}

	private String ensureProtocol(String port)
	{
		if (port.contains("/"))
		{
			return port;
		}
		return port + "/tcp";
	}

	private ContainerInfo getContainerInfo(String id)
	{
		try
		{
			return docker.inspectContainer(id);
		}
		catch (Exception e)
		{
			throw new IllegalStateException(e);
		}
	}
}
