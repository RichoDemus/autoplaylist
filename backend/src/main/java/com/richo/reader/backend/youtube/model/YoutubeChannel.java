package com.richo.reader.backend.youtube.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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

	public YoutubeChannel(String channelName, List<YoutubeVideo> videos)
	{
		this(channelName, new HashSet<>(videos));
	}

	@JsonCreator
	public YoutubeChannel(@JsonProperty("name") String channelName, @JsonProperty("videos") Set<YoutubeVideo> videos, @JsonProperty("lastUpdated") long lastUpdated)
	{
		this.channelName = channelName;
		this.videos = videos;
		this.lastUpdated = Instant.ofEpochSecond(lastUpdated);
	}

	public YoutubeChannel(String channelName, Set<YoutubeVideo> videos, Instant lastUpdated)
	{
		this.channelName = channelName;
		this.videos = videos;
		this.lastUpdated = lastUpdated;
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

	@JsonIgnore
	public Instant getLastUpdated()
	{
		return lastUpdated;
	}

	@JsonProperty("lastUpdated")
	public long getLastUpdatedAsLong()
	{
		return lastUpdated.getEpochSecond();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		YoutubeChannel that = (YoutubeChannel) o;
		return Objects.equals(channelName, that.channelName) &&
				Objects.equals(videos, that.videos);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(channelName, videos);
	}

	@Override
	public String toString()
	{
		return "Channel " + channelName + " with " + videos.size() + " videos";
	}
}
