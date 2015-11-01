package com.richo.reader.backend.youtube.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;


public class YoutubeVideo implements Comparable<YoutubeVideo>
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final String title;

	private final String description;
	private final String videoId;
	private final LocalDateTime uploadDate;
	private final URL url;

	public YoutubeVideo(String title,
						String description,
						String videoId,
						LocalDateTime uploadDate)
	{
		this.title = title;
		this.description = description;
		this.videoId = videoId;
		this.uploadDate = uploadDate;
		this.url = createUrl(videoId);
	}

	@JsonCreator
	public YoutubeVideo(@JsonProperty("title") String title,
						@JsonProperty("description") String description,
						@JsonProperty("videoId") String videoId,
						@JsonProperty("uploadDate") long uploadDate)
	{
		this.title = title;
		this.description = description;
		this.videoId = videoId;
		this.uploadDate = LocalDateTime.ofEpochSecond(uploadDate, 0, ZoneOffset.UTC);
		this.url = createUrl(videoId);
	}

	private URL createUrl(String videoId)
	{
		final String url = "https://www.youtube.com/watch?v=" + videoId;
		try
		{
			return new URL(url);
		}
		catch (MalformedURLException e)
		{
			throw new IllegalArgumentException(url + " is not a valid url", e);
		}
	}

	@Override
	public int compareTo(YoutubeVideo youtubeVideo)
	{
		return this.uploadDate.compareTo(youtubeVideo.uploadDate);
	}

	public String getTitle()
	{
		return title;
	}

	public String getDescription()
	{
		return description;
	}

	public String getVideoId()
	{
		return videoId;
	}

	public URL getUrl()
	{
		return url;
	}

	@JsonIgnore
	public LocalDateTime getUploadDate()
	{
		return uploadDate;
	}

	@JsonProperty("uploadDate")
	public long getUploadDateAsLong()
	{
		return uploadDate.toEpochSecond(ZoneOffset.UTC);
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
		YoutubeVideo that = (YoutubeVideo) o;
		return Objects.equals(title, that.title) &&
				Objects.equals(description, that.description) &&
				Objects.equals(url, that.url);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(title, description, url);
	}

	@Override
	public String toString()
	{
		return title + "(" + videoId + ", " + uploadDate + ")";
	}
}
