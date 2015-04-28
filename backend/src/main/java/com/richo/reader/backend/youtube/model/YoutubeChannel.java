package com.richo.reader.backend.youtube.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;


public class YoutubeChannel
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final String channelName;
	private final Set<YoutubeVideo> videos;

	public YoutubeChannel(String channelName, Set<YoutubeVideo> videos)
	{
		this.channelName = channelName;
		this.videos = videos;
	}

	public String getName()
	{
		return channelName;
	}

	public Set<YoutubeVideo> getVideos()
	{
		return videos;
	}
}
