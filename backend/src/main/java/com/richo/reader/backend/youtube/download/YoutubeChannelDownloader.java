package com.richo.reader.backend.youtube.download;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class YoutubeChannelDownloader
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final YouTube youtube;
	private final String apiKey;

	@Inject
	public YoutubeChannelDownloader(@Named("apiKey") String apiKey)
	{
		this.apiKey = apiKey;
		youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), request -> {
		}).setApplicationName("Richo-Reader").build();
	}

	public Optional<YouTubeVideoChuck> getVideoChunk(String channelName)
	{
		final List<Channel> channels;
		try
		{
			channels = youtube.channels()
					.list("contentDetails")
					.setKey(apiKey)
					.setForUsername(channelName)
					.execute()
					.getItems();
		}
		catch (IOException e)
		{
			logger.error("Unable to find channel {}", channelName, e);
			return Optional.empty();
		}

		if (channels == null)
		{
			logger.error("Got null channels when searching for channel {}", channelName);
			return Optional.empty();
		}

		if (channels.size() == 0)
		{
			logger.error("No such channel: {}", channelName);
			return Optional.empty();
		}

		final Optional<String> playlistId = Optional.of(channels.get(0)).map(this::toPlaylistId);

		if (!playlistId.isPresent())
		{
			logger.error("Did not get a playlistId for channel {}", channelName);
			return Optional.empty();
		}

		return Optional.of(new YouTubeVideoChuck(youtube, playlistId.get(), apiKey));
	}

	private String toPlaylistId(final Channel channel)
	{
		return channel.getContentDetails().getRelatedPlaylists().getUploads();
	}
}
