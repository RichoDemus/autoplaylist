package com.richo.reader.backend.model;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Objects;

public class Item
{
	private final String videoId;
	private final String title;
	private final String description;
	private final URL url;
	private final LocalDateTime uploadDate;

	public Item(String videoId, String title, String description, URL url, LocalDateTime uploadDate)
	{

		this.videoId = videoId;
		this.title = title;
		this.description = description;
		this.url = url;
		this.uploadDate = uploadDate;
	}

	public String getVideoId()
	{
		return videoId;
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
		Item item = (Item) o;
		return Objects.equals(videoId, item.videoId) &&
				Objects.equals(title, item.title) &&
				Objects.equals(description, item.description) &&
				Objects.equals(url, item.url) &&
				Objects.equals(uploadDate, item.uploadDate);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(videoId, title, description, url, uploadDate);
	}

	@Override
	public String toString()
	{
		return com.google.common.base.Objects.toStringHelper(this)
				.add("videoId", videoId)
				.add("title", title)
				.add("description", description)
				.add("url", url)
				.add("uploadDate", uploadDate)
				.toString();
	}
}
