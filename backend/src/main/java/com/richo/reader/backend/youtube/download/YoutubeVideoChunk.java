package com.richo.reader.backend.youtube.download;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//todo this should use the Iterator-pattern instead,
// it's pretty much similiar except you can probably do functional stuff or observables on it
public class YoutubeVideoChunk
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final YouTube youtube;
	private final String playlistId;
	private final String apiKey;

	private Optional<String> nextPageToken;

	public YoutubeVideoChunk(YouTube youtube, String playlistId, String apiKey)
	{
		this.youtube = youtube;
		this.playlistId = playlistId;
		this.apiKey = apiKey;
		nextPageToken = Optional.of("");
	}

	public List<PlaylistItem> getNextVideoChunk()
	{
		if (!nextPageToken.isPresent())
		{
			logger.debug("No more channels for playlistId {}", playlistId);
			return new ArrayList<>();
		}

		final PlaylistItemListResponse playlistItemListResponse;
		try
		{
			playlistItemListResponse = youtube.playlistItems()
					.list("snippet")
					.setKey(apiKey)
					.setPageToken(nextPageToken.get())
					.setPlaylistId(playlistId)
					.setMaxResults(50L)
					.execute();
		}
		catch (IOException e)
		{
			logger.error("Unable to get page {} of playlist {}", nextPageToken, playlistId, e);
			return new ArrayList<>();
		}
		nextPageToken = Optional.ofNullable(playlistItemListResponse.getNextPageToken());
		return playlistItemListResponse.getItems();

	}
}
