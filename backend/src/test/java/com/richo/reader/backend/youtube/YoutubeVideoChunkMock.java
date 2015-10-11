package com.richo.reader.backend.youtube;

import com.google.api.services.youtube.model.PlaylistItem;
import com.richo.reader.backend.youtube.download.YoutubeVideoChunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class YoutubeVideoChunkMock extends YoutubeVideoChunk
{
	private final Queue<List<PlaylistItem>> chunks;

	public YoutubeVideoChunkMock(List<PlaylistItem> items)
	{
		super(null, null, null);
		this.chunks = new LinkedList<>();
		items.forEach(item -> chunks.add(Collections.singletonList(item)));
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

	public int chunksLeft()
	{
		return chunks.size();
	}
}
