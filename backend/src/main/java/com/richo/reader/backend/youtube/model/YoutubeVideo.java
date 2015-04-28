package com.richo.reader.backend.youtube.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Objects;


public class YoutubeVideo implements Comparable<YoutubeVideo>
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final String title;

	private final String description;
	private final String videoId;
	private final LocalDateTime uploadDate;
	private final URL url;

	public YoutubeVideo(String title, String description, String videoId, LocalDateTime uploadDate) throws MalformedURLException
	{
		this.title = title;
		this.description = description;
		this.videoId = videoId;
		this.uploadDate = uploadDate;
		this.url = createUrl(videoId);
	}

	private URL createUrl(String videoId) throws MalformedURLException
	{
		return new URL("https://www.youtube.com/watch?v=" + videoId);
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
