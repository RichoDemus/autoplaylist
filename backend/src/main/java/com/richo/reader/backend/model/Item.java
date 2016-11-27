package com.richo.reader.backend.model;

import java.time.Duration;
import java.util.Objects;

public class Item
{
	private final String id;
	private final String title;
	private final String description;
	private final String uploadDate;
	private final String url;
	private final String duration;
	private final long views;

	public Item(String id,
				String title,
				String description,
				String uploadDate,
				String url,
				Duration duration,
				long views)
	{
		this.id = id;
		this.title = title;
		this.description = description;
		this.uploadDate = uploadDate;
		this.url = url;
		this.duration = durationToString(duration);
		this.views = views;
	}

	private String durationToString(Duration duration)
	{
		return "" + duration.toMinutes() + ":" + toDoubleDigitSeconds(duration.minusMinutes(duration.toMinutes()).getSeconds());
	}

	private String toDoubleDigitSeconds(long seconds)
	{
		final String string = String.valueOf(seconds);
		if (string.length() == 1)
		{
			return "0" + string;
		}
		return string;
	}

	public String getId()
	{
		return id;
	}

	public String getTitle()
	{
		return title;
	}

	public String getDescription()
	{
		return description;
	}

	public String getUploadDate()
	{
		return uploadDate;
	}

	public String getUrl()
	{
		return url;
	}

	public String getDuration()
	{
		return duration;
	}

	public long getViews()
	{
		return views;
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
		return Objects.equals(id, item.id) &&
				Objects.equals(title, item.title) &&
				Objects.equals(description, item.description) &&
				Objects.equals(uploadDate, item.uploadDate) &&
				Objects.equals(url, item.url);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id, title, description, uploadDate, url);
	}
}
