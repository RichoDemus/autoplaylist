package com.richo.reader.backend.youtube;

import com.google.api.services.youtube.model.PlaylistItem;
import com.richo.reader.backend.youtube.download.YouTubeVideoChuck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class YoutubeVideoChunkMock extends YouTubeVideoChuck
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Queue<List<PlaylistItem>> chunks;

	public YoutubeVideoChunkMock(Queue<List<PlaylistItem>> chunks)
	{
		super(null, null, null);
		this.chunks = chunks;
	}

	@Override
	public List<PlaylistItem> getNextVideoChunk()
	{
		final List<PlaylistItem> chunk = chunks.poll();
		if (chunk == null)
		{
			return new ArrayList<>();
		}
		return chunk;

	}
}
