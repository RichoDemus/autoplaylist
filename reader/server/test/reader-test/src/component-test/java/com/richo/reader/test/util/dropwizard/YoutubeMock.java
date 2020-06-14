package com.richo.reader.test.util.dropwizard;

import com.richo.reader.test.util.TestableApplication;
import com.xebialabs.restito.server.StubServer;
import io.restassured.RestAssured;
import org.glassfish.grizzly.http.util.HttpStatus;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.resourceContent;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Condition.get;

public class YoutubeMock implements TestableApplication
{
	private final StubServer server;

	public YoutubeMock()
	{
		server = new StubServer().run();
		RestAssured.port = server.getPort();
		init();
	}

	@Override
	public String getIp()
	{
		return "localhost";
	}

	@Override
	public int getHttpPort()
	{
		return server.getPort();
	}

	@Override
	public int getAdminPort()
	{
		throw new IllegalStateException("Youtube mock does not have an admin port");
	}

	@Override
	public void close()
	{
		server.stop();
	}

	private void init()
	{
		whenHttp(server)
				.match(get("/youtube/v3/channels"))
				.then(status(HttpStatus.OK_200), resourceContent("getChannelResponse.json"));

		whenHttp(server)
				.match(get("/youtube/v3/playlistItems"))
				.then(status(HttpStatus.OK_200), resourceContent("getListItemsResponseTwoVideos.json"));

		whenHttp(server)
				.match(get("/youtube/v3/videos"))
				.then(status(HttpStatus.OK_200), resourceContent("getVideoDetailsResponse.json"));
	}
}
