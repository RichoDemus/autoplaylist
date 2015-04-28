package com.richo.reader.backend.youtube.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Objects;


public class YoutubeVideo implements Comparable<YoutubeVideo>
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public YoutubeVideo(String title, String description, URL url, LocalDateTime uploadDate)
	{
		this.title = title;
		this.description = description;
		this.url = url;
		this.uploadDate = uploadDate;
	}

	private final String title;
	private final String description;
	private final URL url;
	private final LocalDateTime uploadDate;

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

	public URL getUrl()
	{
		return url;
	}

	public LocalDateTime getUploadDate()
	{
		return uploadDate;
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
				Objects.equals(url, that.url) &&
				Objects.equals(uploadDate, that.uploadDate);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(title, description, url, uploadDate);
	}
}
