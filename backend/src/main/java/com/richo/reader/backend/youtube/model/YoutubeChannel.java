package com.richo.reader.backend.youtube.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;


public class YoutubeChannel
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final String channelName;
	private final Set<YoutubeVideo> videos;
	private Instant lastUpdated;

	public YoutubeChannel(String channelName, Set<YoutubeVideo> videos)
	{
		this.channelName = channelName;
		this.videos = videos;
		this.lastUpdated = Instant.now();
	}

	public YoutubeChannel(String channelName, Set<YoutubeVideo> videos, Instant created)
	{
		this.channelName = channelName;
		this.videos = videos;
		this.lastUpdated = created;
	}

	public String getName()
	{
		return channelName;
	}

	public Set<YoutubeVideo> getVideos()
	{
		return videos;
	}

	public void addVideos(Collection<YoutubeVideo> videos)
	{
		this.videos.addAll(videos);
		lastUpdated = Instant.now();
	}

	public Instant getLastUpdated()
	{
		return lastUpdated;
	}
}
