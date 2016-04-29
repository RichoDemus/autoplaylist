package com.richo.reader.test.util;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.NetworkSettings;
import com.spotify.docker.client.messages.PortBinding;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.fail;

public class Container implements AutoCloseable
{
	private static final long MAXIMUM_STARTUP_TIME = 60_000L;
	private final DefaultDockerClient docker;
	private final String id;

	public Container(final String image) throws Exception
	{
		this(image, new HashSet<>());
	}

	public Container(String image, Set<String> env) throws Exception
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

	public void awaitStartup(final BooleanSupplier supplier) throws Exception
	{
		final long start = System.currentTimeMillis();
		boolean isStarted = false;
		while (!isStarted && System.currentTimeMillis() < start + MAXIMUM_STARTUP_TIME)
		{
			isStarted = isRunning(supplier);
			Thread.sleep(100L);
		}
		if (!isStarted)
		{
			fail("Container never started");
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

	public String getIp() throws Exception
	{
		return docker.inspectContainer(id).networkSettings().ipAddress();
	}

	@Override
	public void close() throws Exception
	{
		docker.stopContainer(id, 1);
		docker.removeContainer(id);
		docker.close();
	}

	public Optional<Integer> getExternalPort(String internalPort)
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

	String getLogs()
	{
		try
		{
			return docker.logs(id, DockerClient.LogsParam.stdout()).readFully();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
