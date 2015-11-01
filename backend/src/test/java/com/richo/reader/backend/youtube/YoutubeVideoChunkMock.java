package com.richo.reader.backend.youtube;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.richo.reader.backend.youtube.download.YoutubeVideoChunk;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class YoutubeVideoChunkMock extends YoutubeVideoChunk
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final List<PlaylistItem> chunks;

	public YoutubeVideoChunkMock(List<PlaylistItem> items)
	{
		super(null, null, null);
		this.chunks = new LinkedList<>();
		chunks.addAll(items);
	}

	@Override
	public List<PlaylistItem> getNextVideoChunk()
	{
		final Optional<PlaylistItem> chunk  = getNewestVideo();
		chunk.ifPresent(item ->
		{
			logger.info("returning item {} and removing it from the chunk list", item.getSnippet().getTitle());
			chunks.remove(item);
		});

		return chunk.map(Collections::singletonList)
				.orElseGet(Lists::emptyList);
	}

	private Optional<PlaylistItem> getNewestVideo()
	{
		return chunks.stream().max(this::compareItemPublishDates);
	}

	private int compareItemPublishDates(PlaylistItem item1, PlaylistItem item2)
	{
		return getPublishDateAsLong(item1).compareTo(getPublishDateAsLong(item2));
	}

	private Long getPublishDateAsLong(PlaylistItem item)
	{
		return Optional.of(item)
				.map(PlaylistItem::getSnippet)
				.map(PlaylistItemSnippet::getPublishedAt)
				.map(DateTime::getValue)
				.map(Long::valueOf)
				.get();
	}

	public int chunksLeft()
	{
		return chunks.size();
	}
}
